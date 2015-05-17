# Freezer Simulator
This program simulates a freezer that has a few different kinds of normal and 
anomalous behavior.  The simulator is implemented by having a micro-simulator that emulates
the physics of a freezer.  The simulator has physical state variables (current temperature 
right now) and external control variables (power available, door open, door slightly open).

## Implementation
The controlling variables are simulated by taking the union of several discrete state
machines.  Each of these state machines generates transition events according to a 
simple Markov model. The current state of each of these machines defines the parameters of 
the physical simulation.

### Power Supply
The external power system states are:

NORMAL_POWER - externalPower is 1, transition to POWER_FAIL is exponential time distribution
with mean of months.
POWER_FAIL - externalPower is 0, transition to NORMAL_POWER is mixed exponential with
means of 1 minute and 2 hours.  90% of transitions are quick.

### The Door
The door state machine has the following states:

CLOSED - thermal coupling to outside world is very low.  Transition to OPEN is exponential
with a rate that depends on time of day. When the door is closed, there is a very small chance
that it isn't actually closed, but is left in the previous state until the next time the door
is opened.
OPEN - thermal coupling to outside world is very high.  The door stays open for a time uniformly
distributed between 10 and 30 seconds and then transitions to either CLOSED (98%)
or LEAK (2%).
LEAK - thermal coupling to outside world is low, but not terribly so.  Transition to open
is same as transition from CLOSED.

### Compressor
The physical model involves a compressor that turns on if the temperature is > TEMP_MAX
and the door state is CLOSED or LEAK.  The compressor turns off if the temperature is <TEMP_MIN.

The compressor when on is modeled as a very cold heat sink coupled to the freezer.  The
environment is modeled as a heat source coupled to the freezer.  A simple implementation of
Newton's law is used to compute the freezer temperature over time. The DOOR state controls
the thermal coupling to the outside world and the POWER state controls whether the compressor
actually does any good.

## Discrete Event Simulator
The discrete time simulator maintains a priority queue of upcoming events. To advance, an event
is popped off the queue, time is moved to the time of that event and the `run()` method is called
on that event. In most cases, this causes new events to be scheduled.

This simulator is very simple, but fairly effective and easy to program.  There are three methods
for the simulator 

  `schedule` - allows new events to be scheduled in the future 

  `step` - advances the simulation to the next event in the schedule 

  `nextTransition` - returns the time of the next event to be processed without changing that event.  This 
allows other processing up to that time to be done.