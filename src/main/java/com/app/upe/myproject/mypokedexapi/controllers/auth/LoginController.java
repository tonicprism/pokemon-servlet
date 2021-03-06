package com.app.upe.myproject.mypokedexapi.controllers.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.app.upe.myproject.mypokedexapi.models.auth.User;
import com.app.upe.myproject.mypokedexapi.repositories.UserRepository;
import com.app.upe.myproject.mypokedexapi.services.EncryptPasswordService;
import com.app.upe.myproject.mypokedexapi.services.JWTTokenService;
import com.app.upe.myproject.mypokedexapi.utils.BuildStatusCode;
import com.google.gson.Gson;

@WebServlet("/v1/auth/login")
public class LoginController extends HttpServlet {
  UserRepository userRepository = new UserRepository();
  Gson json = new Gson();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String reqBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    User userFromJson = json.fromJson(reqBody, User.class);
    User user = userRepository.find(userFromJson.getEmail());
    String ERROR_STRING = "Please check the email and password and try again.";
    
    if (user == null) {
      res.sendError(BuildStatusCode.BAD_REQUEST.getValue(),ERROR_STRING);
    } 
    else {
      boolean isPasswordsEquals = EncryptPasswordService.verifyPasswords(userFromJson.getPassword(), user.getPassword());
      if (!isPasswordsEquals) {
        res.sendError(BuildStatusCode.BAD_REQUEST.getValue(),ERROR_STRING);
      }
      else {
        Map<String, String> userInfos = new HashMap<String, String>();
        userInfos.put("userId", user.getId());
        
        try {
          String jwtToken = JWTTokenService.buildJWTToken(userInfos);
          res.sendError(BuildStatusCode.OK.getValue(), jwtToken);
        } catch (Exception e) {
          res.sendError(BuildStatusCode.BAD_REQUEST.getValue(), e.getMessage());
        }
      }
    }
   
  }
    
}
