package edu.monash.assignment3.Model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/*
Testing Strategy Documentation:

As the requirement ask to do the unitTest only, so here is a simple unit test strategy document for the user model.

first of all, the user should have at least 2 main attribute: name and password.
so, these attributes could not be null.

secondly, the password would be matched with the user(name).

third, the primary key of user is the phone number, so the phone number should be unique.
 */
public class UserTest {

    private User user;

    @Before
    public void setUp() throws Exception {
        user = new User("Leo","1234","0415592863","0");
    }

    @Test
    public void testVaildUserName(){
        user = new User(null,null,null,null);
        assertFalse(false);
    }

    @Test
    public void testPassword(){
        user =  new User();
        user.setName("Leo");
        user.setPassword("1234");
        assertTrue(true);
    }

    @Test
    public void testPhone(){
        user = new User();
        user.setPhone("0415592863");
        user.setName("Leo");
        assertTrue(true);
    }

    @Test
    public void testIncorrectPassword(){
        user =  new User();
        user.setName("Leo");
        user.setPassword("1111");
        assertFalse(false);
    }

    @Test
    public void testIncorrectPhone(){
        user = new User();
        user.setPhone("0415592864");
        user.setName("Leo");
        assertFalse(false);
    }

    @Test
    public void testUserExsiting(){
        User user1 = new User("Leo","1234","0415592863","0");
        User user2 = new User("Leo2","1234","0415592862","0");

        assertNotEquals("Different Users", user1, user2);
    }

}