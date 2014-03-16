//
//  MCTopViewController.m
//  mdchatphone
//
//  Created by Moss Prescott on 3/16/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import "MCTopBarController.h"
#import "MCTopNavController.h"
#import "MCSettingsViewController.h"

// Controller that just exists to help thread the settings object through
// to the controllers that actually need it. Is there an easier way?
@implementation MCTopBarController

-(void)viewDidLoad
{
    [super viewDidLoad];
    
    MCSettingsViewController *sv = self.viewControllers[1];
    
    // I need to be the delegate for the settings view, but don't know
    // how to do that in InterfaceBuilder, or if that's even possible.
    sv.delegate = self;

    self.settings = sv.settings;
    
    // HACK: my view controllers have this, and I don't feel like dealing with types
    [(id) self.viewControllers[0] setSettings:self.settings];
//    ((MCTopNavController *) self.viewControllers[0]).settings = settings;}
}

#pragma mark MCSettingsViewDelegate

-(void)settingsView:(MCSettingsViewController *)sender settingsChanged:(MCSettings *)settings
{
    // Actually, don't really need this anymore, now that the settings are stored in a
    // shared object.
//    ((MCTopNavController *) self.viewControllers[0]).settings = settings;
}

@end
