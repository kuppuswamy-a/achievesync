package com.achievesync.userservice.service;

import com.achievesync.userservice.projection.UserProjection;
import com.achievesync.userservice.projection.UserProjectionHandler;
import com.achievesync.userservice.query.FindUserByEmailQuery;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private QueryGateway queryGateway;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            CompletableFuture<UserProjection> userFuture = queryGateway.query(
                new FindUserByEmailQuery(email), UserProjection.class);
            
            UserProjection user = userFuture.get();
            
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }
            
            return new User(user.getEmail(), user.getPasswordHash(), new ArrayList<>());
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with email: " + email, e);
        }
    }
}