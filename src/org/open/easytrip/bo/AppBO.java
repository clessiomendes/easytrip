package org.open.easytrip.bo;

import org.open.easytrip.dao.DAOFactory;


public abstract class AppBO {
	protected BOFactory bos = BOFactory.getInstance();
	protected DAOFactory daos = DAOFactory.getInstance();
}
