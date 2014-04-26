package flapjack.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for AppUtils
 *
 * @author ray
 */
public class AppUtilsTest {

	@Test
	public void testGetDomainName() {
		assertEquals("static.example.com", AppUtils.getDomainName("https://static.example.com/foo/index.html"));
	}
}
