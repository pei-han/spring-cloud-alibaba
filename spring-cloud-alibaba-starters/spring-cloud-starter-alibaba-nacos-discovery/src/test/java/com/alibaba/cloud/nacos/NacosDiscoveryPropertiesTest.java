package com.alibaba.cloud.nacos;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NacosDiscoveryPropertiesTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(NacosDiscoveryPropertiesTest.class);
	private NacosDiscoveryProperties nacosDiscoveryProperties;
	private List<InetAddress> inetAddress;
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		nacosDiscoveryProperties = new NacosDiscoveryProperties();
		NetworkInterface netInterface = NetworkInterface
				.getByName("wlan2");

		inetAddress = Collections.list(netInterface.getInetAddresses());
	}

	@Test
	void testIpv4Address() throws SocketException {
		
		LOGGER.info("ipv4={}",nacosDiscoveryProperties.ipv4Address(inetAddress));
	}

	@Test
	void testIpv6Address() {
		LOGGER.info("ipv6={}",nacosDiscoveryProperties.ipv6Address(inetAddress));
	}

	@Test
	void testIpAddressByIpTypeIsNull() {
		LOGGER.info("ipv4={}",nacosDiscoveryProperties.ipAddressByIpType(inetAddress));
		LOGGER.info("nacosDiscoveryProperties={}",nacosDiscoveryProperties);
	}
	
	@Test
	void testIpAddressByIpTypeIsIpv4() {
		nacosDiscoveryProperties.setIpType("IPv4");
		LOGGER.info("ipv4={}",nacosDiscoveryProperties.ipAddressByIpType(inetAddress));
		LOGGER.info("nacosDiscoveryProperties={}",nacosDiscoveryProperties);
	}
	
	@Test
	void testIpAddressByIpTypeIsIpv6() {
		nacosDiscoveryProperties.setIpType("IPv6");
		LOGGER.info("ipv6={}",nacosDiscoveryProperties.ipAddressByIpType(inetAddress));
		LOGGER.info("nacosDiscoveryProperties={}",nacosDiscoveryProperties);
	}

	@Test
	void testNetworkInterface() throws SocketException {
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements()) {
			LOGGER.info(e.nextElement().toString());
		}
	}
}
