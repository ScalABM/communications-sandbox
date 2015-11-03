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
  * [[acl.CommunicatingActor `CommunicatingActor`]] (i.e., `receiver`) indicating that the `sender` has agreed to
  * perform some action(s) as specified in a [[acl.acts.Request `Request`]] message (ie., `request`) previously sent by
  * the `receiver`.
  *
  * @param conversationId is used to identify a sequence of [[acl.acts.CommunicativeAct `CommunicativeAct`]] messages
  *                       that together form a conversation.
  * @param request is the previously received `request` that has been agreed.
  * @param precondition defines a condition that should be satisfied in order for the `sender` to perform the action(s)
  *                     specified in the agreed `request`.
  * @note The `Agree` message is sent by `sender` using the [[acl.CommunicatingActor.agree `agree`]] action.
  */
case class Agree(conversationId: UUID,
                 request: Request,
                 precondition: (Beliefs) => Boolean) extends CommunicativeAct
