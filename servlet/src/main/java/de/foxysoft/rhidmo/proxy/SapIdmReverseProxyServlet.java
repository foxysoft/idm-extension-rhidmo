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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.message.BasicHttpRequest;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

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
	private void _setXForwardedForHeader(
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
		final String M = "service: ";
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
			String contentType = "";
			Header contentTypeHeader = proxyResponse
					.getFirstHeader("Content-Type");
			if (contentTypeHeader != null) {
				contentType = contentTypeHeader.getValue()
						.toLowerCase();

				LOG.debug(M + "Proxy response Content-Type: {}",
						contentType);

				isXmlOrJsonResponse = contentType
						.startsWith("application/json")
						|| contentType.startsWith("application/xml");
			} else {
				LOG.warn(M
						+ "Missing Content-Type header in proxy response");
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
				// BEGIN: Modification for SapIdmReverseProxyServlet
				// Send transformed content to the client
				transformResponseEntity(proxyResponse,
						servletResponse,
						proxyRequest,
						servletRequest,
						contentType);
			}

		} catch (Exception e) {
			// BEGIN: Modification for SapIdmReverseProxyServlet
			LOG.error(e);
			// END: Modification for SapIdmReverseProxyServlet

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

	/**
	 * Filter out "server" response header, as this seems to be implicitly added
	 * by SAP NetWeaver AS Java to every request, even it such a header already
	 * exists.
	 */
	@Override
	protected void copyResponseHeader(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse,
			Header header) {
		if (false == "server".equals(header.getName())) {
			super.copyResponseHeader(servletRequest,
					servletResponse,
					header);
		}
	}

	/**
	 * Copy response body data (the entity) from the proxy to the servlet
	 * client.
	 */
	private void transformResponseEntity(HttpResponse proxyResponse,
			HttpServletResponse servletResponse,
			HttpRequest proxyRequest,
			HttpServletRequest servletRequest,
			String contentType) throws Exception {
		HttpEntity entity = proxyResponse.getEntity();
		if (entity != null) {
			if (contentType.startsWith("application/json")) {
				transformResponseEntityJson(entity,
						servletResponse,
						proxyRequest,
						servletRequest);
			} else if (contentType.startsWith("application/xml")) {
				transformResponseEntityXml(entity,
						servletResponse,
						proxyRequest,
						servletRequest);
			} else {
				transformResponseEntityOther(entity,
						servletResponse,
						proxyRequest,
						servletRequest);
			}
		}
	}

	/**
	 * Extracts JSON property error.message.value from proxyResponseEntity's
	 * input stream and writes the result to servletResponse's output stream.
	 * 
	 * @param proxyResponseEntity
	 * @param servletResponse
	 * @param proxyRequest
	 * @param servletRequest
	 * @throws IOException
	 */
	private void transformResponseEntityJson(
			HttpEntity proxyResponseEntity,
			HttpServletResponse servletResponse,
			HttpRequest proxyRequest,
			HttpServletRequest servletRequest) throws Exception {
		// TODO: replace with custom JSON handling
		transformResponseEntityOther(proxyResponseEntity,
				servletResponse,
				proxyRequest,
				servletRequest);
	}

	/**
	 * Extracts XML element error/message from proxyResponseEntity's input
	 * stream and writes the result to servletResponse's output stream.
	 * 
	 * @param proxyResponseEntity
	 * @param servletResponse
	 * @param proxyRequest
	 * @param servletRequest
	 * @throws IOException
	 */
	private void transformResponseEntityXml(
			HttpEntity proxyResponseEntity,
			HttpServletResponse servletResponse,
			HttpRequest proxyRequest,
			HttpServletRequest servletRequest) throws Exception {

		final String M = "transformResponseEntityXml: ";
		ServletOutputStream servletResponseOutputStream = servletResponse
				.getOutputStream();
		XmlResponseHandler handler = new XmlResponseHandler(
				servletResponseOutputStream);

		long start = System.currentTimeMillis();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser saxParser = spf.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(handler);
		xmlReader.parse(
				new InputSource(proxyResponseEntity.getContent()));
		servletResponse.setContentLength(handler.getContentLength());
		handler.flush();

		long end = System.currentTimeMillis();
		LOG.debug(M + "XML transformation took {} millis",
				(end - start));
		LOG.debug(M + "handler={}",
				handler);
	}

	/**
	 * Copies all data from proxyResponseEntity's input stream without
	 * modification to servletResponse's output stream.
	 * 
	 * @param proxyResponseEntity
	 * @param servletResponse
	 * @param proxyRequest
	 * @param servletRequest
	 * @throws IOException
	 */
	private void transformResponseEntityOther(
			HttpEntity proxyResponseEntity,
			HttpServletResponse servletResponse,
			HttpRequest proxyRequest,
			HttpServletRequest servletRequest) throws Exception {
		OutputStream servletOutputStream = servletResponse
				.getOutputStream();
		proxyResponseEntity.writeTo(servletOutputStream);
	}

}
