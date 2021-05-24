package com.fmax.prototype.model;

public class ISAN {
	String sISAN;
	
	public ISAN(String sISAN) {
		this.sISAN = sISAN;
	}

	public String getsISAN() {
		return sISAN;
	}

	@Override
	public String toString() {
		return sISAN;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sISAN == null) ? 0 : sISAN.hashCode());
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
		ISAN other = (ISAN) obj;
		if (sISAN == null) {
			if (other.sISAN != null)
				return false;
		} else if (!sISAN.equals(other.sISAN))
			return false;
		return true;
	}
}
