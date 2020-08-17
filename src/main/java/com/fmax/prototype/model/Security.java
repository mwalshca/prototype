package com.fmax.prototype.model;

import java.util.HashMap;
import java.util.Map;

public class Security {
	public final String cusip;	
	
	private Security(String cusip) {
		assert cusip != null;
		this.cusip = cusip;
	}
	
	public static Security getInstance(String cusip) {
		Security sec = securitiesByCusip.get(cusip);
		if( null == sec)
			sec = addSecurity( cusip );
		assert sec != null;
		assert sec.cusip != null;
		assert sec.cusip.equals(cusip); 
		return sec;
	}
	
	static Map<String, Security> securitiesByCusip = new HashMap<>();
	
	synchronized 
	static Security addSecurity(String cusip) {
		return securitiesByCusip.put( cusip, new Security(cusip) );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cusip == null) ? 0 : cusip.hashCode());
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
		Security other = (Security) obj;
		if (cusip == null) {
			if (other.cusip != null)
				return false;
		} else if (!cusip.equals(other.cusip))
			return false;
		return true;
	}	
}
