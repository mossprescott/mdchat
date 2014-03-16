//
//  MCSettingsViewController.m
//  mdchatphone
//
//  Created by Moss Prescott on 3/16/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import "MCSettingsViewController.h"

@interface MCSettingsViewController()

@property (weak, nonatomic) IBOutlet UITextField *serverHost;
@property (weak, nonatomic) IBOutlet UITextField *userName;

@end

@implementation MCSettingsViewController

-(MCSettings *)settings
{
    if (!_settings) {
        _settings = [[MCSettings alloc] init];
        
        _settings.serverHost = @"localhost";
        _settings.serverPort = 9998;
        _settings.userName = @"moss";
    }
    
    return _settings;
}

- (IBAction)serverHostChanged:(id)sender {
    self.settings.serverHost = self.serverHost.text;
    
    [self.delegate settingsView:self settingsChanged:self.settings];
}

- (IBAction)userNameChanged:(id)sender {
    self.settings.userName = self.userName.text;
    
    [self.delegate settingsView:self settingsChanged:self.settings];
}

#pragma mark UITextFieldDelegate

// Dismiss the keyboard when the user is doen with it,
// so the tab bar at the bottom of the screen will be
// accessible.
- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    
    return YES;
}
@end
