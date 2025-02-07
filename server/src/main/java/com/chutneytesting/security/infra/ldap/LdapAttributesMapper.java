package com.chutneytesting.security.infra.ldap;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.server.core.domain.security.Authorization;
import com.chutneytesting.server.core.domain.security.Role;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.springframework.ldap.core.AttributesMapper;

public class LdapAttributesMapper implements AttributesMapper<UserDto> {

    private final Pattern ldapGroupPattern;
    private final LdapAttributesProperties ldapAttributesProperties;
    private final AuthenticationService authenticationService;

    LdapAttributesMapper(LdapAttributesProperties ldapAttributesProperties,
                         String ldapGroupsPattern,
                         AuthenticationService authenticationService) {
        this.ldapGroupPattern = Pattern.compile(ldapGroupsPattern);
        this.ldapAttributesProperties = ldapAttributesProperties;
        this.authenticationService = authenticationService;
    }

    @Override
    public UserDto mapFromAttributes(Attributes attributes) throws NamingException {
        UserDto user = new UserDto();

        user.setId(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getId())));
        user.setName(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getName())));
        user.setFirstname(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getFirstname())));
        user.setLastname(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getLastname())));
        user.setMail(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getMail())));

        List<String> groups = extractAttributeMultiValue(attributes.get(ldapAttributesProperties.getGroups()));
        groups.stream()
            .map(this::applyLdapGroupMatcher)
            .filter(Objects::nonNull)
            .forEach(user::addRole);

        return readRole(user);
    }

    private UserDto readRole(UserDto userDto) {
        UserDto dto = new UserDto(userDto);

        if (dto.getRoles().contains("ADMIN")) {
            Arrays.stream(Authorization.values()).map(Authorization::name).forEach(dto::grantAuthority);
        }

        Role role = authenticationService.userRoleById(dto.getId());
        dto.addRole(role.name);
        role.authorizations.stream().map(Enum::name).forEach(dto::grantAuthority);

        return dto;
    }

    private String applyLdapGroupMatcher(String ldapGroup) {
        Matcher ldapGroupMatcher = ldapGroupPattern.matcher(ldapGroup);
        if (ldapGroupMatcher.matches()) {
            return ldapGroupMatcher.group(1);
        }
        return null;
    }

    private String extractAttributeMonoValue(Attribute attribute) throws NamingException {
        String value = null;

        if (attribute != null) {
            Object attrValue = attribute.get();
            if (attrValue instanceof String) {
                value = (String) attrValue;
            } else {
                value = Objects.toString(attrValue);
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> extractAttributeMultiValue(Attribute attribute) throws NamingException {
        List<T> values = new ArrayList<>();

        if (attribute != null) {
            NamingEnumeration<T> nameValues = (NamingEnumeration<T>) attribute.getAll();
            while (nameValues.hasMoreElements()) {
                T v = nameValues.nextElement();
                if (v != null) {
                    values.add(v);
                }
            }
        }
        return values;
    }
}
