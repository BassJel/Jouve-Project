package com.doculibre.constellio.services;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class AuthenticationServicesImpl implements AuthenticationServices {

	@Override
	public boolean authenticate(String username, String password) {
		UserServices userServices = ConstellioSpringUtils.getUserServices();
		ConstellioUser user = userServices.get(username);
		String passwordHash = ConstellioUser.getHash(password);
		if (user != null && user.getPasswordHash().equals(passwordHash)) {
			return true;
		}
		return false;
	}
}
