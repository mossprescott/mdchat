//
//  MCSettings.h
//  mdchatphone
//
//  Created by Moss Prescott on 3/16/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import <Foundation/Foundation.h>

// Simple model object that holds on to values needed to connect to the server
// and post.
@interface MCSettings : NSObject

@property (nonatomic, strong) NSString *serverHost;
@property (nonatomic) NSInteger serverPort;

@property (nonatomic, strong) NSString *userName;

@end
