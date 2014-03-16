//
//  MCTopNavController.h
//  mdchatphone
//
//  Created by Moss Prescott on 3/16/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MCSettings.h"

// Controller that just exists to help thread the settings object through
// to the controllers that actually need it. Is there an easier way?
@interface MCTopNavController : UINavigationController

@property (nonatomic, strong) MCSettings *settings;

@end
