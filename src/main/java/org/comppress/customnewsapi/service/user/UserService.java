package org.comppress.customnewsapi.service.user;


import org.comppress.customnewsapi.dto.UserDto;
import org.comppress.customnewsapi.exceptions.DuplicateEntryException;
import org.springframework.http.ResponseEntity;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface UserService {

    ResponseEntity<UserDto> saveUser(UserDto userDto) throws DuplicateEntryException;

    ResponseEntity<UserDto> deleteUser();

    ResponseEntity<UserDto> getDeletedUser(String email);
}
