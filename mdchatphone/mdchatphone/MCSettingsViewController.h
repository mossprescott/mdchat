//
//  MCSettingsViewController.h
//  mdchatphone
//
//  Created by Moss Prescott on 3/16/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MCSettings.h"

// Forward declaration for the controller type to be defined shortly.
// Remember, we're talking to a C compiler...
@class MCSettingsViewController;


// I want to let the other controllers listen for changes to the "settings".
// Not entirely sure I'm using a delegate in a sensible way, but this ought
// to work.
@protocol MCSettingsViewDelegate <NSObject>

-(void)settingsView:(MCSettingsViewController *)sender settingsChanged:(MCSettings *)settings;

@end


// Finally, the actual controller.
@interface MCSettingsViewController : UIViewController<UITextFieldDelegate>

@property (nonatomic, strong) MCSettings *settings;

@property (nonatomic, strong) id<MCSettingsViewDelegate> delegate;

@end


