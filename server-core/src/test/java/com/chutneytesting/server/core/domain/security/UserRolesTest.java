package com.chutneytesting.server.core.domain.security;

import static com.chutneytesting.server.core.domain.security.User.userByRoleNamePredicate;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.arbitraries.SetArbitrary;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserRolesTest {

    @Test
    void minimal_build_should_have_default_role() {
        UserRoles defaultBuild = UserRoles.builder().build();
        UserRoles nullBuild = UserRoles.builder().withUsers(null).withRoles(null).build();
        UserRoles emptyBuild = UserRoles.builder().withUsers(emptySet()).withRoles(emptySet()).build();

        for (UserRoles userRole : List.of(defaultBuild, nullBuild, emptyBuild)) {
            Assertions.assertThat(userRole.roles()).containsExactly(Role.DEFAULT);
            Assertions.assertThat(userRole.users()).isEmpty();
        }
    }

    @Test
    void roles_must_contains_default_role() {
        assertThatThrownBy(() ->
            UserRoles.builder()
                .withRoles(singleton(Role.builder().withName("roleName").build()))
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void users_must_have_a_declared_role() {
        assertThatThrownBy(() ->
            UserRoles.builder()
                .withUsers(List.of(User.builder().withRole("UNDECLARED_ROLE").build()))
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void keep_first_user_role_when_user_has_many() {
        UserRoles sut = UserRoles.builder()
            .withRoles(List.of(
                Role.builder().withName("R").build(),
                Role.DEFAULT
            ))
            .withUsers(List.of(
                User.builder().withId("id").withRole("R").build(),
                User.builder().withId("id").withRole(Role.DEFAULT.name).build()
            ))
            .build();

        Assertions.assertThat(sut.users())
            .hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("id", "id")
            .hasFieldOrPropertyWithValue("roleName", "R");
    }

    @Test
    void should_find_user_by_id() {
        User user2 = User.builder().withId("user2").build();
        UserRoles sut = UserRoles.builder()
            .withUsers(List.of(User.builder().withId("user1").build(), user2))
            .build();

        Assertions.assertThat(sut.userById("user2")).hasValue(user2);
        Assertions.assertThat(sut.userById("userX")).isEmpty();
    }

    @Test
    void should_find_users_by_role_name() {
        Role role2 = Role.builder().withName("role2").build();
        User user2 = User.builder().withId("user2").withRole(role2.name).build();
        Role unknown_role = Role.builder().withName("unknown_role").build();
        UserRoles sut = UserRoles.builder()
            .withRoles(List.of(Role.DEFAULT, role2))
            .withUsers(List.of(User.builder().withId("user1").build(), user2))
            .build();

        Assertions.assertThat(sut.usersByRole(role2)).containsExactly(user2);
        Assertions.assertThat(sut.usersByRole(unknown_role)).isEmpty();
    }

    @Test
    void should_find_role_by_name() {
        Role role2 = Role.builder().withName("role2").build();
        UserRoles sut = UserRoles.builder()
            .withRoles(List.of(Role.DEFAULT, Role.builder().withName("role1").build(), role2))
            .build();

        Assertions.assertThat(sut.roleByName("role2")).isEqualTo(role2);
        assertThatThrownBy(() ->
            sut.roleByName("roleX")
        ).isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    void should_add_new_user_by_id() {
        UserRoles sut = UserRoles.builder()
            .withUsers(List.of(User.builder().withId("user1").build(), User.builder().withId("user2").build()))
            .build();

        User newUser = sut.addNewUser("new-user");

        assertThat(newUser.id).isEqualTo("new-user");
        Assertions.assertThat(sut.users()).contains(newUser);
    }

    @Property
    void should_keep_users_and_roles_orders_when_build(@ForAll("validRoles") Set<Role> roles) {
        roles.add(Role.DEFAULT);
        Set<User> users = PropertyBasedTestingUtils.validUsers(roles);

        List<Role> orderedRoles = List.copyOf(roles);
        List<User> orderedUsers = List.copyOf(users);

        UserRoles sut = UserRoles.builder()
            .withRoles(roles)
            .withUsers(users)
            .build();

        Assertions.assertThat(sut.roles()).containsExactlyElementsOf(orderedRoles);

        assertThat(Role.authorizations(List.copyOf(sut.roles())))
            .containsExactlyElementsOf(Role.authorizations(orderedRoles));

        sut.roles().forEach(role -> {
            List<User> usersForRole = orderedUsers.stream().filter(userByRoleNamePredicate(role.name)).collect(toList());
            Assertions.assertThat(sut.usersByRole(role)).containsExactlyElementsOf(usersForRole);
        });
    }

    @Provide
    @SuppressWarnings("unused")
    private SetArbitrary<Role> validRoles() {
        return PropertyBasedTestingUtils.validRole().set().ofMinSize(1).ofMaxSize(10);
    }

}
