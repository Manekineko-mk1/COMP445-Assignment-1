# COMP445_Assignment1
COMP 445 - Networking - Assignment 1

Introduction
In this assignment, you will implement a simple HTTP client application and experiment it in real HTTP Servers (web servers). 
Before starting on this assignment, it is strongly recommend that you read the provided programming samples and review the 
associated course materials.

Outline
The following is a summary of the main tasks of the Assignment:
1. Setup your development and testing environment.
2. Study HTTP network protocol specifications.
3. Build your own HTTP client library.
4. Program your HTTP client application (curl command).
5. (optional) Implement more HTTP protocol specifications.
6. (optional) Enhance the functionalities of the HTTP client.

Objective
The goal of this Lab is to have your fist steps in implementing network protocol from its technical specifications. 
Generally, the specifications of network protocol (for example FTP for file transfer and NTP for time synchronization) are 
provided by standardization organizations like IETF (http://www.ietf.org).

A protocol is a system of rules that allow two or more entities of a communications system to transmit information via any 
kind of variation of a physical quantity. These are the rules or standard that defines the syntax, semantics and 
synchronization of communication and possible error recovery methods. Protocols may be implemented by hardware, software, 
or a combination of both. [1] Network protocols standards are used for various purposes and different OSI - TCP/IP models layers. 
In the context of this Lab, we focus on implementing as subset HTTP network protocol specifications on top of the transport layer, 
TCP/UDP of OSI, TCP/IP models respectively.The Hypertext Transfer Protocol (HTTP) is an application protocol for distributed, 
collaborative, hypermedia information systems. HTTP is the foundation of data communication for the World Wide Web. [2]
For this purpose, you are requested to implement the basic functionalities of cURL command line, 
the functionalities that are related to HTTP protocol.
