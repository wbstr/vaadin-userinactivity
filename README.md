# UserInactivity Add-on for Vaadin 7

An extension to track user activity, and provide inactivity timeout event.

What the extension counts as a user activity (or action) is simple:
It's a user interaction followed by an uidl request. 
 
For example user action is
 - when a user clicks on a button
 - when a user changes an immediate field's value
 - when a user switch tabsheet
 - when a user navigates between views
 
For example NOT a user action - except in case of polling
 - when a user moves the mouse
 - when a user clicks on a disabled component
 - when a user clicks on a non-active component. (background image, or layout background)
 - when a user selects text in a label
 
## About polling.
According to the defintion of user action, the behavior differs with polling.
If you use polling with vaadin UI.setPollionInterval (or with a custom way), every case 
listed under 'NOT a user action' becames a user action.
 
## About Push.
This extension is not affected by push. 
Does not use push, even if available.

## Implementation details.
Both timeout and activity tracking is implemented on client side.
Timeout is handled with a GWT timer. On user action the timer is cancelled.
User action tracked by MOUSEDOWN, KEYDOWN, TOUCHSTART events,
and a delayed rpc method called to signal the event to server side.


