package com.fmax.prototype.model;

public class ISIN {
	String sISIN;
	
	public ISIN(String sISAN) {
		this.sISIN = sISAN;
	}

	public String getsISAN() {
		return sISIN;
	}

	@Override
	public String toString() {
		return sISIN;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sISIN == null) ? 0 : sISIN.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ISIN other = (ISIN) obj;
		if (sISIN == null) {
			if (other.sISIN != null)
				return false;
		} else if (!sISIN.equals(other.sISIN))
			return false;
		return true;
	}
}
