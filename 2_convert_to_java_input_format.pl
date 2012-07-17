#!/usr/bin/perl -w
use strict;

my $labeled_seed_file = "./data/A_seeds.txt";
my $test_file = "./data/A_test.txt";
#my $unlabeled_file = shift; # optional

my $outputfile = "./data/java_input1.txt";  # labeled_seeds
my $outputfile2 = "./data/java_input2.txt"; # test + unlabeled
my $outputfile3 = "./data/java_input3.txt"; # feature lookup table

open OUT1, ">$outputfile" or die;
open OUT2, ">$outputfile2" or die;
open OUT3, ">$outputfile3" or die;


open IN1, "$labeled_seed_file" or die;
open IN2, "$test_file" or die;

my %f_lookup;   # 1 if feature exists
my %f_index;    # value = index of feature

while (<IN1>) {
    chomp;
    my @tokens = split /\s+/, $_;
    foreach my $i (1..$#tokens) {
        $f_lookup{"$tokens[$i]"} = 1;
    }
}

while (<IN2>) {
    chomp;
    my @tokens = split /\s+/, $_;
    foreach my $i (1..$#tokens) {
        $f_lookup{"$tokens[$i]"} = 1;
    }
}
close IN1;
close IN2;

my @ranked_f = sort (keys %f_lookup);
my $idx = 0;
foreach my $f (@ranked_f) {
    $f_index{"$f"} = $idx;
    print OUT3 $f."\n";
    $idx ++;
}

close OUT3;

open IN1, "$labeled_seed_file" or die;
open IN2, "$test_file" or die;
while (<IN1>) {
    chomp;
    my @tokens = split /\s+/, $_;
    print OUT1 $tokens[0]."\t";
    foreach my $i (1..$#tokens) {
        print OUT1 $f_index{"$tokens[$i]"}." ";
    }
    print OUT1 "\n";
}

while (<IN2>) {
    chomp;
    my @tokens = split /\s+/, $_;
    print OUT2 $tokens[0]."\t";
    foreach my $i (1..$#tokens) {
        print OUT2 $f_index{"$tokens[$i]"}." ";
    }
    print OUT2 "\n";
}
close IN1;
close IN2;
close OUT1;
close OUT2;
