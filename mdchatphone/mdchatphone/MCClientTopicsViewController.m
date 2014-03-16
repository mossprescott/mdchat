//
//  MCClientTopicTVC.m
//  mdchatphone
//
//  Created by Moss Prescott on 3/15/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import "MCClientTopicsViewController.h"

// Overrides the "generic" topics controller to implement fetching topics from the server.
@implementation MCClientTopicsViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    
    [self fetchTopics];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    // Reload each time, just in case the settings changed, say:
    [self fetchTopics];
}

- (void)insertNewTopic:(id)sender
{
    NSURL* url = [NSURL URLWithString:[NSString stringWithFormat:@"http://%@:%d/topics/topic%d",
                                       self.settings.serverHost, self.settings.serverPort, [self.topics count]]];
    
    dispatch_queue_t fetchQueue = dispatch_queue_create("add topic", NULL);
    dispatch_async(fetchQueue, ^{
        NSData *jsonResult = [NSData dataWithContentsOfURL:url];
        
        if (jsonResult) {
            NSError *error = nil;
            NSDictionary *newTopic = [NSJSONSerialization JSONObjectWithData:jsonResult options:0 error:&error];
            
            NSLog(@"created topic: %@", newTopic);
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self fetchTopics];
            });
        }
    });
}

-(IBAction)refresh
{
    [self fetchTopics];
}

-(void)fetchTopics
{
    [self.refreshControl beginRefreshing];

    NSURL* url = [NSURL URLWithString:[NSString stringWithFormat:@"http://%@:%d/topics",
                                       self.settings.serverHost, self.settings.serverPort]];
    
    dispatch_queue_t fetchQueue = dispatch_queue_create("fetch topics", NULL);
    dispatch_async(fetchQueue, ^{
        NSData *jsonResult = [NSData dataWithContentsOfURL:url];
        
        if (jsonResult) {
            NSError *error = nil;
            NSArray *topics = [NSJSONSerialization JSONObjectWithData:jsonResult options:0 error:&error];
            
            NSLog(@"loaded topics: %@", topics);
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.refreshControl endRefreshing];
                self.topics = topics;
            });
        }
        else {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.refreshControl endRefreshing];
                self.topics = @[];
            });
        }
    });
}

@end
