package Org::Zeromeaner::Zero;

use strict;
use warnings;

use XML::Simple;
use Log::Log4perl;

sub new {
	my $class = shift;
	
	my $self = bless {}, $class;

	my ($dir) = @_;

	$self->{dir} = $dir;

	$self->{"log"} = Log::Log4perl->get_logger("org.zeromeaner.zero");

	return $self;
}

sub dir {
	return shift->{dir};
}

sub version {
	my $self = shift;
	my $pom = (new XML::Simple)->XMLin($self->{dir} . "zeromeaner-parent/pom.xml");
	return $pom->{version};
}

sub is_snapshot {
	return (shift->version) =~ /-SNAPSHOT$/;
}


sub _wd_system {
	my $self = shift;
	my $dir = shift;
	
	my $pid = fork();
	if($pid == 0) {
		chdir $dir;
		exec @_;
	} else {
		waitpid $pid, 0;
	}
}

sub pull {
	my $self = shift;
	
	$self->_wd_system($self->dir, "git pull");
}

sub mvn {
	my $self = shift;
	my %args = (module => "zeromeaner-parent");
	if(ref $_[0]) {
		%args = (%args, %{shift @_});
	}
	
	$self->_wd_system($args{module}, "mvn", @_);
}

1;
