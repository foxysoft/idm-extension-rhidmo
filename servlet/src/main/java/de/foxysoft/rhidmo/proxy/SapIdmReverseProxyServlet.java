/*******************************************************************************
 * Copyright MITRE
 * Copyright 2017 Lambert Boskamp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package de.foxysoft.rhidmo.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.message.BasicHttpRequest;

import de.foxysoft.rhidmo.Log;

public class SapIdmReverseProxyServlet
		extends org.mitre.dsmiley.httpproxy.ProxyServlet {
	private static final long serialVersionUID = 1L;
	private static final Log LOG = Log
			.get(SapIdmReverseProxyServlet.class);

	/**
	 * Copy of org.mitre.dsmiley.httpproxy.ProxyServlet.setXForwardedForHeader,
	 * as that one is private and hence cannot be used in a subclass.
	 * 
	 * @param servletRequest
	 * @param proxyRequest
	 */
	protected void _setXForwardedForHeader(
			HttpServletRequest servletRequest,
			HttpRequest proxyRequest) {
		if (doForwardIP) {
			String forHeaderName = "X-Forwarded-For";
			String forHeader = servletRequest.getRemoteAddr();
			String existingForHeader = servletRequest
					.getHeader(forHeaderName);
			if (existingForHeader != null) {
				forHeader = existingForHeader + ", " + forHeader;
			}
			proxyRequest.setHeader(forHeaderName,
					forHeader);

			String protoHeaderName = "X-Forwarded-Proto";
			String protoHeader = servletRequest.getScheme();
			proxyRequest.setHeader(protoHeaderName,
					protoHeader);
		}
	}

	/**
	 * This method is a modified copy of
	 * 
	 * org.mitre.dsmiley.httpproxy.ProxyServlet.service(HttpServletRequest,
	 * HttpServletResponse)
	 * 
	 * The original code is licensed under the Apache License, version 2.0.
	 * Credits go to the original authors and copyright owners, David Smiley and
	 * MITRE.
	 */
	@Override
	protected void service(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse)
			throws ServletException, IOException {
		// initialize request attributes from caches if unset by a subclass by
		// this point
		if (servletRequest.getAttribute(ATTR_TARGET_URI) == null) {
			servletRequest.setAttribute(ATTR_TARGET_URI,
					targetUri);
		}
		if (servletRequest.getAttribute(ATTR_TARGET_HOST) == null) {
			servletRequest.setAttribute(ATTR_TARGET_HOST,
					targetHost);
		}

		// Make the Request
		// note: we won't transfer the protocol version because I'm not sure it
		// would truly be compatible
		String method = servletRequest.getMethod();
		String proxyRequestUri = rewriteUrlFromRequest(servletRequest);
		HttpRequest proxyRequest;
		// spec: RFC 2616, sec 4.3: either of these two headers signal that
		// there is a message body.
		if (servletRequest.getHeader(HttpHeaders.CONTENT_LENGTH) != null
				|| servletRequest.getHeader(
						HttpHeaders.TRANSFER_ENCODING) != null) {
			proxyRequest = newProxyRequestWithEntity(method,
					proxyRequestUri,
					servletRequest);
		} else {
			proxyRequest = new BasicHttpRequest(method,
					proxyRequestUri);
		}

		copyRequestHeaders(servletRequest,
				proxyRequest);

		// Use local copy of setXForwardedForHeader, which behaves
		// the same as the private super method.
		_setXForwardedForHeader(servletRequest,
				proxyRequest);

		HttpResponse proxyResponse = null;
		try {
			// Execute the request
			proxyResponse = doExecute(servletRequest,
					servletResponse,
					proxyRequest);

			// Process the response:

			// Pass the response code. This method with the "reason phrase" is
			// deprecated but it's the
			// only way to pass the reason along too.
			int statusCode = proxyResponse.getStatusLine()
					.getStatusCode();

			// BEGIN: Modification for SapIdmReverseProxyServlet
			boolean isXmlOrJsonResponse = false;
			Header contentTypeHeader = proxyResponse
					.getFirstHeader("Content-Type");
			if (contentTypeHeader != null) {
				String contentType = contentTypeHeader.getValue()
						.toLowerCase();
				isXmlOrJsonResponse = contentType
						.startsWith("application/json")
						|| contentType.startsWith("application/xml");
			}
			if (statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR
					&& isXmlOrJsonResponse) {
				servletResponse.setStatus(HttpServletResponse.SC_OK);
			} else {
				// noinspection deprecation
				servletResponse.setStatus(statusCode,
						proxyResponse.getStatusLine()
								.getReasonPhrase());
			}
			// END: Modification for SapIdmReverseProxyServlet

			// Copying response headers to make sure SESSIONID or other Cookie
			// which comes from the remote
			// server will be saved in client when the proxied url was
			// redirected to another one.
			// See issue
			// [#51](https://github.com/mitre/HTTP-Proxy-Servlet/issues/51)
			copyResponseHeaders(proxyResponse,
					servletRequest,
					servletResponse);

			if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
				// 304 needs special handling. See:
				// http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
				// Don't send body entity/content!
				servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH,
						0);
			} else {
				// Send the content to the client
				copyResponseEntity(proxyResponse,
						servletResponse,
						proxyRequest,
						servletRequest);
			}

		} catch (Exception e) {
			// abort request, according to best practice with HttpClient
			if (proxyRequest instanceof AbortableHttpRequest) {
				AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
				abortableHttpRequest.abort();
			}
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			if (e instanceof ServletException)
				throw (ServletException) e;
			// noinspection ConstantConditions
			if (e instanceof IOException)
				throw (IOException) e;
			throw new RuntimeException(e);

		} finally {
			// make sure the entire entity was consumed, so the connection is
			// released
			if (proxyResponse != null)
				consumeQuietly(proxyResponse.getEntity());
			// Note: Don't need to close servlet outputStream:
			// http://stackoverflow.com/questions/1159168/should-one-call-close-on-httpservletresponse-getoutputstream-getwriter
		}
	}

	@Override
	public void log(String m) {
		LOG.debug(m);
	}

	@Override
	public void log(String m,
			Throwable t) {
		LOG.error(m);
		LOG.error(t);
	}

}
