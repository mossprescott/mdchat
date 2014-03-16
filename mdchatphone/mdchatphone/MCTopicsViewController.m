//
//  MCMasterViewController.m
//  mdchatphone
//
//  Created by Moss Prescott on 3/14/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import "MCTopicsViewController.h"

#import "MCPostsViewController.h"

@interface MCTopicsViewController ()

@end

@implementation MCTopicsViewController

//- (void)awakeFromNib
//{
//    [super awakeFromNib];
//}

-(void)setTopics:(NSArray *)topics
{
    _topics = topics;
    
    NSLog(@"reload");
    
    [self.tableView reloadData];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    self.navigationItem.leftBarButtonItem = self.editButtonItem;

    UIBarButtonItem *addButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(insertNewTopic:)];
    self.navigationItem.rightBarButtonItem = addButton;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)insertNewTopic:(id)sender
{
    // overridden in the subclass
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.topics.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Topic Cell" forIndexPath:indexPath];

    NSDictionary *topic = self.topics[indexPath.row];
    
    cell.textLabel.text = [topic valueForKey:@"name"];
    
    NSNumber *postCount = [topic valueForKey:@"messageCount"];
    if (postCount.intValue == 1) {
        cell.detailTextLabel.text = @"1 post";
    }
    else {
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%@ posts", postCount];
    }
    
    return cell;
}

//- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    // Return NO if you do not want the specified item to be editable.
//    return YES;
//}

//- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    if (editingStyle == UITableViewCellEditingStyleDelete) {
//        [self.topics removeObjectAtIndex:indexPath.row];
//        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
//    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
//        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view.
//    }
//}

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"showTopic"]) {
        NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
        NSDictionary *topic = self.topics[indexPath.row];
        [[segue destinationViewController] setTopicName:[topic valueForKey:@"name"]];
        [[segue destinationViewController] setSettings:self.settings];
    }
}

@end
