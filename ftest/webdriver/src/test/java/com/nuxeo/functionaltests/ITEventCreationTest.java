/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Sun Seng David TAN
 *     Florent Guillaume
 *     Antoine Taillefer
 *     Mincong Huang
 */
package com.nuxeo.functionaltests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.functionaltests.Constants.WORKSPACES_PATH;
import static org.nuxeo.functionaltests.Constants.WORKSPACES_TITLE;
import static org.nuxeo.functionaltests.Constants.WORKSPACE_TYPE;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.functionaltests.AbstractTest;
import org.nuxeo.functionaltests.Locator;
import org.nuxeo.functionaltests.RestHelper;
import org.nuxeo.functionaltests.pages.DocumentBasePage;
import org.nuxeo.functionaltests.pages.DocumentBasePage.UserNotConnectedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Agenda tests.
 */
public class ITEventCreationTest extends AbstractTest {

    private final static String USERNAME = "jdoe";

    private final static String PASSWORD = "test";

    private final static String EVENTS_WORKSPACE_TITLE = "events";

    private final static String EVENTS_WORKSPACE_PATH = WORKSPACES_PATH + EVENTS_WORKSPACE_TITLE;

    @Before
    public void createUserAndWorkspaceForEvents() throws UserNotConnectedException {
        RestHelper.createUser(USERNAME, PASSWORD, USERNAME, "lastname1", "company1", "email1", "members");
        RestHelper.createDocument(WORKSPACES_PATH, WORKSPACE_TYPE, EVENTS_WORKSPACE_TITLE, null);
        RestHelper.addPermission(EVENTS_WORKSPACE_PATH, USERNAME, "Everything");
    }

    @After
    public void cleanup() throws DocumentBasePage.UserNotConnectedException {
        RestHelper.cleanup();
    }

    @Test
    public void testEventCreation() throws Exception {
        DocumentBasePage page = login(USERNAME, PASSWORD);
        DocumentBasePage eventPage = createTestEvent(page);
        assertEquals("My Event", eventPage.getCurrentDocumentTitle());
        logout();
    }

    protected DocumentBasePage createTestEvent(DocumentBasePage page) throws IOException {
        page.getContentTab().goToDocument(WORKSPACES_TITLE).getContentTab().goToDocument(EVENTS_WORKSPACE_TITLE);

        EventCreationFormPage eventCreationFormPage = page.getContentTab().getDocumentCreatePage("Event",
                EventCreationFormPage.class);

        return eventCreationFormPage.createEventDocument("My Event", "Event description", "1/1/2300 12:00 PM",
                "1/1/2300 12:00 PM");
    }

    @Test
    public void testEventsWidget() throws UserNotConnectedException, IOException {
        DocumentBasePage page = login(USERNAME, PASSWORD);
        createTestEvent(page);

        page.getUserHome().goToDashboard();

        WebElement betweenBanner = Locator.findElementWithTimeout(By.id("betweenBanner"));
        assertEquals("Incoming Events", betweenBanner.getText());
        assertTrue(driver.findElement(By.id("agenda")).getText().contains("My Event"));
        driver.switchTo().defaultContent();

        logout();
    }
}
