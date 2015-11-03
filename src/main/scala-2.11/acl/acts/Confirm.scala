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


/** A message sent from some [[acl.CommunicatingActor `CommunicatingActor`]] (i.e., `sender`) to another
  * [[acl.CommunicatingActor `CommunicatingActor`]] (i.e., `receiver`) indicating that a `proposition` is true, where
  * the `receiver` is known to, at a minimum, be uncertain about the truth value of the `proposition`.
  *
  * @param conversationId is an expression used to identify a sequence of communicative acts that together form a
  *                       conversation.
  * @param proposition is a proposition that the `sender` believes to be true, and intends that the `receiver` also
  *                    comes to believe to be true.
  * @note The `Confirm` message is sent by the `sender` using the [[acl.CommunicatingActor.confirm `confirm`]] action.
  */
case class Confirm(conversationId: UUID, proposition: Beliefs) extends CommunicativeAct
