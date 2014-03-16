//
//  MCDetailViewController.h
//  mdchatphone
//
//  Created by Moss Prescott on 3/14/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MCSettings.h"

@interface MCPostsViewController : UIViewController<UITextFieldDelegate, UITableViewDataSource>

@property (nonatomic, strong) MCSettings *settings;

@property (strong, nonatomic) NSString *topicName;
@property (strong, nonatomic) NSArray *messages;

-(void)doPost:(NSString *)text;

@end
