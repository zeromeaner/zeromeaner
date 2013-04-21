#!/usr/bin/perl

use strict;
use warnings;
use Getopt::Long qw(:config gnu_getopt);
use Log::Log4perl qw(:easy);
use Switch;

use lib __FILE__ =~ /(.*\/)/;
use Org::Zeromeaner::Zero;

my %opt = (volume => 0);

__FILE__ =~ /(.*)\//;
$opt{dir} = "$1/../";

GetOptions(\%opt, 
	"v|verbose" => sub { $opt{volume}++; },
	"q|quiet" => sub { $opt{volume}--; },
	"p|pull" => \$opt{pull}
);

Log::Log4perl->easy_init({
	level => $INFO - 10000 * $opt{volume},
	layout => '%m%n'
});

my $zero = new Org::Zeromeaner::Zero($opt{dir});

INFO("0mino version " . $zero->version . " running from " . $zero->dir);

$zero->pull if($opt{pull});

foreach my $cmd (@ARGV) {
	switch($cmd) {
		case "build" { $zero->mvn("clean", "install"); }
		case "build-server" {
			my $profile = $zero->is_snapshot ? "shade" : "shade-stable";
			$zero->mvn({module => "zeromeaner-server"}, "package", "-P", $profile) 
		}
		case "run-server" {
			open(LS, "ls " . $opt{dir} . "zeromeaner-server/target/*-all-*.jar|sort -V -r|");
			my $jar;
			while($jar = <LS>) {
				chomp $jar;
				last if($zero->is_snapshot and $jar =~ /-SNAPSHOT/);
				last if(!($zero->is_snapshot) and $jar !~/-SNAPSHOT/);
				$jar = undef;
			}
			close(LS);
			if(defined $jar) {
				my $port = $zero->is_snapshot ? 61898 : 61897;
				system("java", "-jar", $jar, "--nogui", "--port", $port);
			} else {
				ERROR("Could not find runnable jar.");
			}
		}
		else { die "unknown command:$cmd"; }
	}
}
