package com.nuxeo.functionaltests;

import java.io.IOException;

import org.nuxeo.functionaltests.pages.AbstractPage;
import org.nuxeo.functionaltests.pages.DocumentBasePage;
import org.nuxeo.functionaltests.pages.FileDocumentBasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @since 5.8
 */
public class EventCreationFormPage extends AbstractPage {

    @FindBy(id = "document_create:nxl_vevent:nxw_title")
    public WebElement titleTextInput;

    @FindBy(id = "document_create:nxl_vevent:nxw_vevent_location")
    public WebElement placeTextInput;

    @FindBy(id = "document_create:nxl_vevent:nxw_vevent_dtstartInputDate")
    public WebElement startDateTextInput;

    @FindBy(id = "document_create:nxl_vevent:nxw_vevent_dtendInputDate")
    public WebElement endDateTextInput;

    @FindBy(id = "document_create:nxw_documentCreateButtons_CREATE_DOCUMENT")
    public WebElement createButton;

    public EventCreationFormPage(WebDriver driver) {
        super(driver);
    }

    public DocumentBasePage createEventDocument(String title, String place,
            String startDate, String endDate) throws IOException {
        titleTextInput.sendKeys(title);
        placeTextInput.sendKeys(place);
        startDateTextInput.sendKeys(startDate);
        endDateTextInput.sendKeys(endDate);
        createButton.click();
        return asPage(DocumentBasePage.class);
    }
}
