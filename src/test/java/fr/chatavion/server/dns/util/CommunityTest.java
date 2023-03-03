package fr.chatavion.server.dns.util;

import fr.chatavion.server.utils.Community;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommunityTest {

    @Test
    void findCommunityThrowExceptionIfNull() {
        Assertions.assertThrows(NullPointerException.class, () -> Community.findCommunity(null));
    }

    @Test
    void createCommunityThrowExceptionIfNull() {
        Assertions.assertThrows(NullPointerException.class, () -> Community.createCommunity(null));
    }
}