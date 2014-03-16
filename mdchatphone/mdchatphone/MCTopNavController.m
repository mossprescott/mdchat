//
//  MCTopNavController.m
//  mdchatphone
//
//  Created by Moss Prescott on 3/16/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import "MCTopNavController.h"
#import "MCTopicsViewController.h"

// Controller that just exists to help thread the settings object through
// to the controllers that actually need it. Is there an easier way?
@implementation MCTopNavController

-(void)viewDidLoad
{
    [super viewDidLoad];
    
    // HACK: my view controllers have this, and I don't feel like dealing with types
    [(id) self.topViewController setSettings:self.settings];
}
@end
