# UserInactivity Add-on for Vaadin 7

An extension to track user activity, and inactivity timeout.

What the extension counts as a user activity (or action)?
It's a user interaction followed by an uidl request. 
 
For example user action is
 - when a user clicks on a button
 - when a user changes an immediate field's value
 - when a user changes a tab in a tabsheet
 - when a user navigates between views
 
For example NOT a user action - see polling for exceptions
 - when a user moves the mouse
 - when a user clicks on a disabled component
 - when a user clicks on a non-active component. (background image, or layout background)
 - when a user selects text in a label
 
## Implementation details.
Both timeout and activity tracking is implemented on client side.
Timeout is handled with a GWT timer. On user action the timer is cancelled.
User action tracked by MOUSEDOWN, KEYDOWN, TOUCHSTART events,
and a delayed rpc method called to signal the event to server side.

## About polling.
According to the poor defintion of user action, and to the dumb implementation, the behavior differs with polling.
If you use polling by vaadin UI.setPollionInterval (or a custom way), every event seen at implemtation details counts as a user action.
 
## About Push.
This extension is not affected by push. 
Does not use push, even if available.

## Usage

The extension supports two modes of inactivity counting. For UI, and for Session.

### inactivity timeout per UI
````
@Override
protected void init(VaadinRequest request) {
    UserInactivityExtension userInactivityExtension = UserInactivityExtension.init(this);
    userInactivityExtension.addTimeoutListener(new UserInactivityExtension.TimeoutListener() {
        Notification.show("Inactivity report sent to your boss");
    });
    userInactivityExtension.scheduleTimeout(120);
    ...
````


### inactivity timeout per VaadinSession
````
@Override
protected void init(VaadinRequest request) {
    UserInactivityExtension userInactivityExtension = UserInactivityExtension.init(this);
    SessionTimeoutHandler sessionTimeoutHandler = userInactivityExtension.initSessionTimeoutHandler();
    sessionTimeoutHandler.addTimeoutListener(new SessionTimeoutHandler.SessionTimeoutListener() {

        @Override
        public void timeout() {
            Notification.show("Inactivity report sent to your boss");
        }
    });
    sessionTimeoutHandler.start(120);    
    ...
````

### Real use cases

Instead of a useless notification, you might want to pop-up a count-down window.
It's easy to mix this extension with (countdownclock)[https://vaadin.com/directory#addon/countdownclock] addon.

See [DemoUI.java](userinactivity-demo/src/main/java/com/wcs/vaadin/userinactivity/demo/DemoUI.java) for example.

## Online demo

Try the add-on demo at http://demo.webstar.hu/userinactivity

