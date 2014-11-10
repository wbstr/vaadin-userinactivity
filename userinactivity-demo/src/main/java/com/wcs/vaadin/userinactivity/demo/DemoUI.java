package com.wcs.vaadin.userinactivity.demo;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.wcs.vaadin.userinactivity.UserInactivityExtension;
import com.wcs.vaadin.userinactivity.SessionTimeoutHandler;
import java.util.Calendar;
import org.vaadin.kim.countdownclock.CountdownClock;
import org.vaadin.kim.countdownclock.CountdownClock.EndEventListener;

@Title("ClientInactivity Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

    private SessionTimeoutHandler sessionTimeoutHandler;
    private static final int SESSION_TIMEOUT = 10;
    private static final int COUNT_DOWN_TIMEOUT = 5;
    private CountDownWindow countDownWindow;

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "com.wcs.vaadin.userinactivity.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(new Label("A window will pop up, when no action in "+SESSION_TIMEOUT+" second."));
        layout.addComponent(new Button("Action"));
        setContent(layout);
        UserInactivityExtension userInactivityExtension = UserInactivityExtension.init(this);
        sessionTimeoutHandler = userInactivityExtension.initSessionTimeoutHandler(SESSION_TIMEOUT);
        sessionTimeoutHandler.addTimeoutListener(new SessionTimeoutHandler.SessionInactivityTimeoutListener() {

            @Override
            public void timeout() {
                openCountDownWindow();
            }
        });
        userInactivityExtension.addActionListener(new UserInactivityExtension.ActionListener() {

            @Override
            public void action() {
                Notification.show("OK, "+SESSION_TIMEOUT+" seconds from now");
                closeCountDownWindow();
            }
        });
    }
    
    private void openCountDownWindow() {
        countDownWindow = new CountDownWindow();
        addWindow(countDownWindow);
    }

    private void closeCountDownWindow() {
        if (countDownWindow != null) {
            removeWindow(countDownWindow);
            countDownWindow = null;
        }
    }

    private class CountDownWindow extends Window {

        private final VerticalLayout layout;

        public CountDownWindow() {
            setCaption("You were inactive");
            setResizable(false);
            setClosable(false);
            center();
            layout = new VerticalLayout();
            setContent(layout);
            buildLayout();
        }

        private void buildLayout() {
            layout.setMargin(true);
            layout.setSpacing(true);
            CountdownClock clock = new CountdownClock();
            Calendar c = Calendar.getInstance();
            c.add(Calendar.SECOND, COUNT_DOWN_TIMEOUT + sessionTimeoutHandler.getRemainingSeconds());
            clock.setDate(c.getTime());
            clock.setFormat("<span style='font: bold 25px Arial; margin: 10px'>"
                            + "You will be logged out in %s seconds.</span>");
            clock.addEndEventListener(new EndEventListener() {
                @Override
                public void countDownEnded(CountdownClock clock) {
                    closeCountDownWindow();
                    if (sessionTimeoutHandler.getRemainingSeconds() < COUNT_DOWN_TIMEOUT) {
                        Notification.show("Imagine you are logged out!", Notification.Type.ERROR_MESSAGE);
                    } else if (sessionTimeoutHandler.getRemainingSeconds() < 1) {
                        openCountDownWindow();
                    } else {
                        Notification.show("User action initiated on an other browser tab.");
                        sessionTimeoutHandler.reschedule();
                    }
                }
            });
            layout.addComponent(clock);
            layout.addComponent(new Button("Extend session"));
        }

    }

}
