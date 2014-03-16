//
//  MCDetailViewController.m
//  mdchatphone
//
//  Created by Moss Prescott on 3/14/14.
//  Copyright (c) 2014 Moss Prescott. All rights reserved.
//

#import "MCPostsViewController.h"

@interface MCPostsViewController ()

@property (weak, nonatomic) IBOutlet UITextField *postText;
@property (weak, nonatomic) IBOutlet UIButton *postButton;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
//@property (weak, nonatomic) IBOutlet UILabel *detailDescriptionLabel;

- (void)configureView;
@end

@implementation MCPostsViewController

#pragma mark - Managing the detail item

- (void)setTopicName:(NSString *)newName
{
    NSLog(@"setTopicName %@", newName);
    
    if (_topicName != newName) {
        _topicName = newName;
        
        // Update the view.
        [self configureView];
    }
}

-(void)setMessages:(NSArray *)messages
{
    _messages = messages;
    
    [self.tableView reloadData];
}

- (void)configureView
{
    // Update the user interface for the detail item.

    if (self.topicName) {
        NSLog(@"configure: %@", self.topicName);
//        self.detailDescriptionLabel.text = self.topicName;
        self.navigationItem.title = self.topicName;
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    [self configureView];
}

- (IBAction)postTouched:(id)sender {
    NSString *text = self.postText.text;
    
    self.postText.text = @"";
    [self.postText resignFirstResponder];
    [self syncButtonState];
    
    if (text.length > 0) {
        [self doPost:text];
    }
}

-(void)syncButtonState
{
    self.postButton.enabled = self.postText.text.length > 0;
}

-(void)doPost:(NSString *)text
{
    // Overridden in the subclass
}

#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

-(int)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.messages.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Reverse the order of messages, so the newest message shows up at the top, under the text field:
    NSInteger index = self.messages.count - indexPath.row - 1;
    NSDictionary *message = self.messages[index];
    
    UITableViewCell *cell;
    NSString *text;
    Boolean isMyPost = [message[@"user"] isEqualToString:self.settings.userName];
    if (isMyPost) {
        cell = [tableView dequeueReusableCellWithIdentifier:@"My Message Cell" forIndexPath:indexPath];
        
        text = message[@"text"];
    }
    else {
        cell = [tableView dequeueReusableCellWithIdentifier:@"Other Message Cell" forIndexPath:indexPath];
        
        text = [NSString stringWithFormat:@"%@: %@", message[@"user"], message[@"text"]];
    }
    
    // HACK: too lazy to write a controller (if that's even the right thing)
    UIView *scrollView = cell.subviews[0];
    UIView *contentView = scrollView.subviews[1];
    UILabel *label = contentView.subviews[0];
    label.text = text;
    
    return cell;
}

#pragma mark UITextFieldDelegate

- (IBAction)textChanged:(id)sender {
    [self syncButtonState];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    
    [self postTouched:nil];
    
    return YES;
}

@end
