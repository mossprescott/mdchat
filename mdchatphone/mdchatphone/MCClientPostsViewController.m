//
//  MCClientTopicViewController.m
//  mdchatphone
//
//  Created by Moss Prescott on 3/16/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import "MCClientPostsViewController.h"

@implementation MCClientPostsViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    
    // This controller is not a UITableViewController, so it seems InterfaceBuilder
    // won't help with this and instead I do it here manually. But that doesn't work
    // because there's UITableViewController anywhere that I can find.
    // Punting for now:
//    UIRefreshControl *refreshControl = [[UIRefreshControl alloc] init];
//    refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:@"Pull to refresh"];
////    self.tableView.refreshControl = refreshControl;
//    [refreshControl addTarget:self action:@selector(fetchMessages)
//             forControlEvents:UIControlEventValueChanged];
    
//    // HACK
//    self.serverHost = @"localhost";
//    self.serverPort = 9998;
//    self.user = @"moss";
    
    [self fetchMessages];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    // Reload each time, just in case the settings changed, say:
    [self fetchMessages];
}

-(void)doPost:(NSString *)text
{
    // Note: not sure how to do a POST with an NSURL, so just using the same hack
    // to post with GET which is also useful for driving the service with a browser.
    
    NSURLComponents *urlComponents = [NSURLComponents new];
    urlComponents.scheme = @"http";
    urlComponents.host = self.settings.serverHost;
    urlComponents.port = @(self.settings.serverPort);  // wrap as NSNumber?
    urlComponents.path = @"/post";

    // Note: bug here--special cahracters such as '&' are not encoded properly in
    // the 'text' parameter. It doesn't look like there's a good, correct
    // implementation in the libs.
    urlComponents.query = [NSString stringWithFormat:@"user=%@&topic=%@&text=%@",
                           self.settings.userName, self.topicName, text];
    
    NSURL* url = [urlComponents URL];
    
    NSLog(@"url: %@", [url absoluteString]);
    
    dispatch_queue_t fetchQueue = dispatch_queue_create("post", NULL);
    dispatch_async(fetchQueue, ^{
        /*NSData *jsonResult =*/ [NSData dataWithContentsOfURL:url];
        
//        NSError *error = nil;
//        NSDictionary *topic = [NSJSONSerialization JSONObjectWithData:jsonResult options:0 error:&error];
//        NSArray *messages = topic[@"messages"];
        
        NSLog(@"posted");
        
        dispatch_async(dispatch_get_main_queue(), ^{
            //            [self.refreshControl endRefreshing];
            [self fetchMessages];
        });
    });
}

-(void)fetchMessages
{
//    [self.refreshControl beginRefreshing];
    
    NSURL* url = [NSURL URLWithString:[NSString stringWithFormat:@"http://%@:%d/topics/%@",
                                       self.settings.serverHost, self.settings.serverPort, self.topicName]];
    
    dispatch_queue_t fetchQueue = dispatch_queue_create("fetch messages", NULL);
    dispatch_async(fetchQueue, ^{
        NSData *jsonResult = [NSData dataWithContentsOfURL:url];
        
        NSError *error = nil;
        NSDictionary *topic = [NSJSONSerialization JSONObjectWithData:jsonResult options:0 error:&error];
        NSArray *messages = topic[@"messages"];
        
        NSLog(@"loaded messages: %@", messages);
        
        dispatch_async(dispatch_get_main_queue(), ^{
//            [self.refreshControl endRefreshing];
            self.messages = messages;
        });
    });
}

@end
