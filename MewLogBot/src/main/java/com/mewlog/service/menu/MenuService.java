package com.mewlog.service.menu;


public interface MenuService {
	  
	Runnable getAction(String action, long chatId, String firstName);
    
}
