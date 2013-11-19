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
 */
package com.nuxeo.functionaltests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.functionaltests.AbstractTest;
import org.nuxeo.functionaltests.fragment.GadgetsContainerFragment;
import org.nuxeo.functionaltests.pages.DocumentBasePage;
import org.nuxeo.functionaltests.pages.DocumentBasePage.UserNotConnectedException;
import org.nuxeo.functionaltests.pages.FileDocumentBasePage;
import org.nuxeo.functionaltests.pages.UserHomePage;
import org.nuxeo.functionaltests.pages.admincenter.usermanagement.UsersGroupsBasePage;
import org.nuxeo.functionaltests.pages.admincenter.usermanagement.UsersTabSubPage;
import org.nuxeo.functionaltests.pages.forms.FileCreationFormPage;
import org.nuxeo.functionaltests.pages.tabs.AccessRightsSubPage;
import org.nuxeo.functionaltests.pages.tabs.SummaryTabSubPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Agenda tests.
 */
public class ITEventCreationTest extends AbstractTest {

    private final static String USERNAME = "jdoe";

    private final static String PASSWORD = "test";

    private final static String WORKSPACE_TITLE = "events";

    @Before
    public void createUserAndWorkspaceForEvents() throws UserNotConnectedException {
        login();

        UsersGroupsBasePage page;
        UsersTabSubPage usersTab = login().getAdminCenter().getUsersGroupsHomePage().getUsersTab();
        usersTab = usersTab.searchUser(USERNAME);
        if (!usersTab.isUserFound(USERNAME)) {
            page = usersTab.getUserCreatePage().createUser(USERNAME, USERNAME,
                    "lastname1", "company1", "email1", PASSWORD, "members");
            usersTab = page.getUsersTab(true);
        } // search user usersTab =
        usersTab.searchUser(USERNAME);
        assertTrue(usersTab.isUserFound(USERNAME));

        // create a new wokspace and grant all rights to the test user
        DocumentBasePage documentBasePage = usersTab.exitAdminCenter().getHeaderLinks().getNavigationSubPage().goToDocument(
                "Workspaces");
        DocumentBasePage workspacePage = createWorkspace(documentBasePage,
                WORKSPACE_TITLE, "");
        AccessRightsSubPage accessRightSubTab = workspacePage.getManageTab().getAccessRightsSubTab();
        // Need WriteSecurity (so in practice Manage everything) to edit a
        // Workspace
        if (!accessRightSubTab.hasPermissionForUser("Manage everything",
                USERNAME)) {
            accessRightSubTab.addPermissionForUser(USERNAME,
                    "Manage everything", true);
        }

        logout();
    }

    @After
    public void cleanup()
            throws DocumentBasePage.UserNotConnectedException {
        login();
        driver.findElement(By.linkText("Workspaces")).click();
        asPage(DocumentBasePage.class).getContentTab().removeAllDocuments();
        logout();
    }

    @Test
    public void testEventCreation() throws Exception {
        DocumentBasePage page = login(USERNAME, PASSWORD);
        DocumentBasePage eventPage = createTestEvent(page);
        assertEquals("My Event", eventPage.getCurrentDocumentTitle());
        logout();
    }

    protected DocumentBasePage createTestEvent(DocumentBasePage page) throws IOException {
        page.getContentTab().goToDocument(
                "Workspaces").getContentTab().goToDocument(WORKSPACE_TITLE);

        EventCreationFormPage eventCreationFormPage = page.getContentTab().getDocumentCreatePage(
                "Event", EventCreationFormPage.class);

        return eventCreationFormPage.createEventDocument(
                "My Event", "Event description", "1/1/2000 12:00 PM", "1/1/2300 12:00 PM");
    }

    @Test
    public void testAgendaGadget() throws UserNotConnectedException, IOException {
        DocumentBasePage page = login(USERNAME, PASSWORD);
        createTestEvent(page);

        UserHomePage userHome = page.getUserHome();
        userHome = userHome.goToDashboard();

        // add the gadget
        waitUntilElementPresent(By.id("addGadgetButton"));
        WebElement addGadgetButton = driver.findElement(By.id("addGadgetButton"));
        addGadgetButton.click();
        // wait for fancybox
        userHome.getFancyBoxContent();

        driver.switchTo().defaultContent();
        driver.switchTo().frame("fancybox-frame");
        driver.findElement(By.xpath("//div[@gadget-name='agenda']")).click();
        driver.switchTo().defaultContent();

        // test the content
        GadgetsContainerFragment gadgetsFragment = getWebFragment(userHome.gadgetsContainer,
                GadgetsContainerFragment.class);
        gadgetsFragment.waitForGadgetsLoad("content");

        WebElement agendaFrame = driver.findElement(By.cssSelector(".gwt-Frame.agenda"));
        driver.switchTo().defaultContent();
        WebDriver agendaDriver = driver.switchTo().frame(agendaFrame);

        assertEquals("Incoming events", agendaDriver.findElement(By.id("betweenBanner")).getText());
        assertTrue(agendaDriver.findElement(By.id("agenda")).getText().contains("My Event"));

        logout();
    }
}
