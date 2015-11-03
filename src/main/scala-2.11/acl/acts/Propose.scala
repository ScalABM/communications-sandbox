/*
Copyright 2015 David R. Pugh, Dan F. Tang, J. Doyne Farmer

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
*/
package acl.acts

import java.util.UUID

import acl.Beliefs

/** A message from some [[acl.CommunicatingActor `CommunicatingActor`]] (i.e., `sender`) to another
  * [[acl.CommunicatingActor `CommunicatingActor`]] (i.e., `receiver`) representing a proposal to perform a certain
  * action, given certain preconditions.
  *
  * @param conversationId is an expression used to identify a sequence of communicative acts that together form a
  *                       conversation.
  * @param content is an action expression defining the action(s) that the `sender` is proposing to perform in the
  *                event that the `precondition` is satisfied.
  * @param precondition is a proposition defining the precondition that should be satisfied in order for the `sender`
  *                     to perform the action(s) specified in the `content`.
  * @note The `Propose` message is sent by the `sender` using the [[acl.CommunicatingActor.propose `propose`]] method.
  */
case class Propose(conversationId: UUID,
                   content: Any,
                   precondition: (Beliefs) => Boolean) extends CommunicativeAct
