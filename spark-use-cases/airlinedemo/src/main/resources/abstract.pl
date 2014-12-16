#!/usr/bin/perl
use strict;
use warnings;
     
my $file = $ARGV[0] or die "Need to get CSV file on the command line\n";
     
my $sum = 0;
my $cancelled=0;
my $origin="NA";
my $totalnbofcancelled = 200;
my $totalnbofrecords = 1000;
my $nbofcancelled = 0;
my $nbofrecords = 0;

open(my $data, '<', $file) or die "Could not open '$file' $!\n";
open(my $output,'>',"abstract.csv");
     
while (my $line = <$data>) {
 chomp $line;
     
 my @fields = split "," , $line;
 $cancelled=$fields[21];
 $origin=$fields[16];


 print "cancelled = " . $cancelled . "\n";

 if ($cancelled eq "0" && $origin eq "ORD"){
   print "non cancelled" . $nbofrecords . "\n";
   print $output $line . "\n";
   $nbofrecords++;

   if ($nbofrecords > $totalnbofrecords)
   {
     exit;
   }

 }
 else
 {
   print "cancelled" . $nbofcancelled . "\n";
   $nbofcancelled++;
   if ($nbofcancelled < $totalnbofcancelled)
   {
      print $output $line . "\n";
   }

 }

 }
