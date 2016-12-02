package de.foxysoft.rhidmo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class SequentialDelegationClassLoader extends ClassLoader {

	private static class CompoundEnumeration<E>
			implements Enumeration<E> {
		private Enumeration<E>[] enums;
		private int index = 0;

		public CompoundEnumeration(Enumeration<E>[] enums) {
			this.enums = enums;
		}

		private boolean next() {
			while (index < enums.length) {
				if (enums[index] != null
						&& enums[index].hasMoreElements()) {
					return true;
				}
				index++;
			}
			return false;
		}

		public boolean hasMoreElements() {
			return next();
		}

		public E nextElement() {
			if (!next()) {
				throw new NoSuchElementException();
			}
			return enums[index].nextElement();
		}
	}

	private ClassLoader[] m_delegates = null;

	public SequentialDelegationClassLoader(ClassLoader... delegates) {
		super(null);
		m_delegates = delegates;
	}

	@Override
	public Class<?> loadClass(String name)
			throws ClassNotFoundException {
		Class<?> result = null;
		for (int i = 0; i < m_delegates.length && result == null; ++i) {
			try {
				result = m_delegates[i].loadClass(name);
			} catch (ClassNotFoundException e) {
			}
		}
		if (result != null) {
			return result;
		} else {
			throw new ClassNotFoundException(name);
		}
	}

	@Override
	public URL getResource(String name) {
		URL result = null;
		for (int i = 0; i < m_delegates.length && result == null; ++i) {
			result = m_delegates[i].getResource(name);
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<URL> getResources(String name)
			throws IOException {
		Enumeration<URL> result = null;
		ArrayList<Enumeration<URL>> tmp = new ArrayList<Enumeration<URL>>(
				m_delegates.length);
		IOException e = null;

		for (int i = 0; i < m_delegates.length; ++i) {
			try {
				tmp.add(m_delegates[i].getResources(name));
			} catch (IOException ie) {
				e = ie;
			}
		}

		result = new CompoundEnumeration<URL>(
				(Enumeration<URL>[]) tmp.toArray());
		if (result.hasMoreElements() || e == null) {
			return result;
		} else {
			throw e;
		}
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream result = null;
		for (int i = 0; i < m_delegates.length && result == null; ++i) {
			result = m_delegates[i].getResourceAsStream(name);
		}
		return result;
	}
}
