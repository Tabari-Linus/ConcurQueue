# ConcurQueueSystem

ConcurQueue is  a high-performance job dispatcher system, designed to handle a large number of concurrent jobs efficiently. It is built using Java core concueeency primitives.

## Components of ConcurQueue
- **Task**: Represents a unit of work that can be executed.
- **TaskProducer**: Responsible for creating and submitting tasks to the queue.
- **TaskConsumer**: Consumes tasks from the queue and executes them.
- **TaskQueue**: A thread-safe queue that holds tasks to be processed.
- **RetryWorker**: Handles retry logic for failed tasks, ensuring they are retried a specified number of times before being discarded.
- **SystemMonitor**: Monitors the system in real-time, providing insights into task processing, queue status, system health and exporting Json reports.
- **Concurrency Demonstration**: Implemented to demonstrate concurrency conditions such as deadlock, Race conditions, and Synchronization solutions.

