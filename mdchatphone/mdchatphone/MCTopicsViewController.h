//
//  MCMasterViewController.h
//  mdchatphone
//
//  Created by Moss Prescott on 3/14/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MCSettings.h"

@interface MCTopicsViewController : UITableViewController

@property (nonatomic, strong) MCSettings *settings;

@property (nonatomic, strong) NSArray *topics;  // NSDictionaries containing the JSON

@end
