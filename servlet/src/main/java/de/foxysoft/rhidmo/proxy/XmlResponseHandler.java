package de.foxysoft.rhidmo.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import de.foxysoft.rhidmo.Log;

public class XmlResponseHandler extends DefaultHandler {
	private static final Log LOG = Log.get(XmlResponseHandler.class);
	private static final String MESSAGE_ELEMENT_NAME = "message";
	private OutputStreamWriter m_outputStreamWriter = null;
	private int m_contentLength = 0;

	public XmlResponseHandler(OutputStream outputStream) {
		m_outputStreamWriter = new OutputStreamWriter(outputStream,
				// TODO: use encoding from proxy response Content-Type header
				Charset.forName("UTF-8"));
	}

	private boolean m_inMessageElement = false;

	@Override
	public void startElement(String uri,
			String localName,
			String qName,
			Attributes attributes) {
		if (MESSAGE_ELEMENT_NAME.equals(localName)) {
			m_inMessageElement = true;
		}
	}

	@Override
	public void characters(char[] ch,
			int start,
			int length) {
		if (m_inMessageElement) {
			try {
				m_outputStreamWriter.write(ch,
						start,
						length);

				// TODO: Find a more efficient solution for book keeping
				// of content length, creating a new String for each chunk is
				// wasteful;
				// Maybe using chunked encoding is the best approach?
				m_contentLength += new String(ch, start, length)
						.getBytes(Charset.forName("UTF-8")).length;

			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}

	@Override
	public void endElement(String uri,
			String localName,
			String qName) {
		if (MESSAGE_ELEMENT_NAME.equals(localName)) {
			m_inMessageElement = false;
		}
	}

	public int getContentLength() {
		return m_contentLength;
	}

	public void flush() throws IOException {
		m_outputStreamWriter.flush();
	}

	@Override
	public String toString() {
		return "Content-Length: " + m_contentLength;
	}

}
