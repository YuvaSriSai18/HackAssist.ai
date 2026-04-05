package com.hackassist.ai.Service;

import com.hackassist.ai.dto.CurrentUserDTO;

public interface IAuthService {
    
    CurrentUserDTO getCurrentUser(String username);
    
    Boolean isUserAuthenticated(String username);
}
