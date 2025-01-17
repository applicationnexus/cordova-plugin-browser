#import "BrowserPlugin.h"

@implementation BrowserPlugin {
    SFSafariViewController *_safariViewController;
}

- (void)handleOpenURLWithApplicationSourceAndAnnotation:(NSNotification*)notification {
    NSDictionary*  notificationData = [notification object];

    if ([notificationData isKindOfClass: NSDictionary.class]) {
        NSURL* url = notificationData[@"url"];
        NSString* sourceApplication = notificationData[@"sourceApplication"];

        if ([url isKindOfClass:NSURL.class] && [sourceApplication isKindOfClass:NSString.class]) {
            if ([sourceApplication isEqual:@"com.apple.SafariViewService"]) {
                if (_safariViewController) {
                    [_safariViewController dismissViewControllerAnimated:NO completion:nil];
                    [self safariViewControllerDidFinish:_safariViewController];
                }
            }
        }
    }
}

- (void)ready:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *result;
    if ([SFSafariViewController class]) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                   messageAsString:@"SFSafariViewController is not available"];
    }
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


- (void)open:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* result = nil;
    
    NSString* appUrlString = [command argumentAtIndex:0];
    NSString* browserUrlString = [command argumentAtIndex:1];
    
    if (appUrlString == nil || browserUrlString == nil) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Incorrect numbers of arguments"];
    } else {
        NSURL* appUrl = [NSURL URLWithString:appUrlString];
        NSURL* browserUrl = [NSURL URLWithString:browserUrlString];
        
        if ([[UIApplication sharedApplication] canOpenURL:appUrl]) {
            [[UIApplication sharedApplication] openURL:appUrl];
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:appUrlString];
        } else {
            [[UIApplication sharedApplication] openURL:browserUrl];
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:browserUrlString];
        }
    }
    
    [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
}

- (void)onLoad:(CDVInvokedUrlCommand *)command {
    self.loadCallbackId = command.callbackId;
}

- (void)onClose:(CDVInvokedUrlCommand *)command {
    self.closeCallbackId = command.callbackId;
}

# pragma mark - SFSafariViewControllerDelegate

- (void)safariViewController:(SFSafariViewController *)controller didCompleteInitialLoad:(BOOL)didLoadSuccessfully {
    if (self.loadCallbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:self.loadCallbackId];
    }
}

- (void)safariViewControllerDidFinish:(SFSafariViewController *)controller {
    _safariViewController = nil;

    if (self.closeCallbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:self.closeCallbackId];
    }
}

@end
