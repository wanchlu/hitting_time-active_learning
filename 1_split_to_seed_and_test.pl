#!/usr/bin/perl -w
use strict;

my $input = shift;
my $seed_size = shift;
my $test_size = shift;

`shuffle.pl < $input > ./data/temp`;
`head -n$seed_size ./data/temp > data/A_seeds.txt`;

my $sum = $seed_size + $test_size;
`head -n$sum ./data/temp | tail -n$test_size > data/A_test.txt`;
