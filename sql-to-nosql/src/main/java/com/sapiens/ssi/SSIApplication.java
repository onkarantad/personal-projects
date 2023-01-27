package com.sapiens.ssi;

import com.sapiens.ssi.logging.InitLogging;
import com.sapiens.ssi.service.*;

public class SSIApplication {
	static ServiceInterface service = new ServiceImpl();

	public static void main(String[] args) {
		// Initilize logging and set log properties.
		InitLogging.setLogProperties();

		// Call the service method
		service.serviceMethod();

	}

}
