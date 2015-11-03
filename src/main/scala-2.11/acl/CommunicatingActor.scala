/*
Copyright 2015 David R. Pugh, Dan F. Tang, J. Doyne Farmer

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
*/
package acl

import java.util.UUID

import acl.acts._
import akka.actor.{ActorRef, Actor}

import scala.collection.immutable
import scala.reflect.runtime.universe._


/** Trait defining the behavior of a `CommunicatingActor`. */
trait CommunicatingActor extends Actor {

  /** Accept a previously received proposal from another `CommunicatingActor`.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` whose `proposal` has been accepted.
    * @param proposal is the previously received [[acl.acts.Propose `Propose`]] message that has been accepted.
    * @note `acceptProposal` is a general-purpose acceptance of a previously received [[acl.acts.Propose `Propose`]]
    *       message. The `CommunicatingActor` sending the [[acl.acts.AcceptProposal `AcceptProposal`]] message informs
    *       the `receiver` that it intends that the `receiver` act in accordance with the terms of the `proposal`.
    */
  def acceptProposal(conversationId: UUID, receiver: ActorRef, proposal: Propose): Unit = {
    receiver ! AcceptProposal(conversationId, proposal)
  }

  /** Agree to perform some action (possibly in the future) for another `CommunicatingActor`.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` whose `request` has been agreed.
    * @param request is the previously received [[acl.acts.Request `Request`]] that has been agreed.
    * @param precondition is a proposition that must be satisfied in order for the `CommunicatingActor` to perform
    *                     the `request`.
    * @note `agree` is a general purpose agreement to a previously received [[acl.acts.Request `Request`]] message to
    *       perform certain actions given that a `precondition` is satisfied. The `CommunicatingActor` sending the
    *       [[acl.acts.Agree `Agree`]] message informs the `receiver` that it intends to act in accordance with the 
    *       terms of the  `request`.
    */
  def agree(conversationId: UUID,
            receiver: ActorRef,
            request: Request,
            precondition: (Beliefs) => Boolean): Unit = {
    receiver ! Agree(conversationId, request, precondition)
  }

  /** Request a proposal from another `CommunicatingActor` to perform certain actions (under certain preconditions).
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` that should receive a call for proposal.
    * @param content is an action expression defining the action(s) that the `CommunicatingActor` is requesting the
    *                `receiver` to submit a proposal to perform.
    * @param precondition is a proposition defining conditions that any submitted proposal must satisfy in order to be
    *                     accepted.
    */
  def callForProposal(conversationId: UUID,
                      receiver: ActorRef,
                      content: Any,
                      precondition: (Propose) => Boolean): Unit = {
    receiver ! CallForProposal(conversationId, content, precondition)
  }

  /** Cancel a request that was previously submitted to another `CommunicatingActor`.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` to notify to notify of the cancellation.
    * @param request is the previously received [acl.acts.Request `Request`] that has been cancelled.
    * @note The `cancel` act allows a `CommunicatingActor` to inform the `receiver` that it no longer intends that
    *       the `receiver` perform a previously requested action. This is not the same thing as a
    *       `CommunicatingActor` informing the `receiver` to stop performing an action.  In order for a
    *       `CommunicatingActor` to stop the `receiver` from performing an action it should send a
    *       [[acl.acts.Request `Request`]] message that the `receiver` stop performing that action.
    */
  def cancel(conversationId: UUID, receiver: ActorRef, request: Request): Unit = {
    receiver ! Cancel(conversationId, request)
  }

  /** Confirm for another `CommunicatingActor` that some proposition is true.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` to inform that the `proposition` is true.
    * @param proposition is a proposition that the `CommunicatingActor` believes to be true, and intends that the
    *                    `receiver` also comes to believe to be true.
    * @note The `confirm` act indicates that the `CommunicatingActor`:
    *
    *       1. believes that the `proposition` is true,
    *
    *       2. intends for the `receiver` to also believe that the `proposition` is true,
    *
    *       3. believes that the `receiver` believes that the `proposition` is false (or, at a minimum, believes that
    *       the `receiver` is uncertain about the truth value of the `proposition`).
    *
    *       Properties 1 and 2 require that the `CommunicatingActor` is "sincere." Property 3 determines whether the
    *       `CommunicatingActor` should use the `confirm` vs `inform` vs `disconfirm` action. Note that whether or
    *       not the `receiver` does indeed come to believe that the proposition is true will depend on the beliefs of
    *       the `receiver` concerning the sincerity and reliability of the `CommunicatingActor` sending the
    *       [[acl.acts.Confirm `Confirm`]] message.
    */
  def confirm(conversationId: UUID, receiver: ActorRef, proposition: Beliefs): Unit = {
    receiver ! Confirm(conversationId, proposition)
  }

  /** Confirm for another `CommunicatingActor` that some proposition is false.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the collection of actors that are being notified that the `CommunicatingActor` has
    *                 dis-confirmed the `proposition`.
    * @param proposition is a proposition that the `CommunicatingActor` believes to be false, and intends that the
    *                    `receiver` also come to believe to be false.
    * @note The `disconfirm` act indicates that the `CommunicatingActor`
    *
    *       1. believes that the `proposition` is false
    *
    *       2. intends for the `receiver` to also believe that the `proposition` is false
    *
    *       3. believes that the `receiver` believes that the `proposition` is true (or, at a minimum, believes that
    *       the `receiver` is uncertain about the truth value of the `proposition`).
    *
    *       Properties 1 and 2 require that the `CommunicatingActor` is "sincere." Property 3 determines whether the
    *       `CommunicatingActor` should use the `confirm` vs `inform` vs `disconfirm` action. Note that whether or
    *       not the `receiver` does indeed come to believe that the proposition is false will depend on the beliefs of
    *       the `receiver` concerning the sincerity and reliability of the `CommunicatingActor` sending the
    *       [[acl.acts.Disconfirm `Disconfirm`]] message.
    */
  def disconfirm(conversationId: UUID, receiver: ActorRef, proposition: Beliefs): Unit = {
    receiver ! Disconfirm(conversationId, proposition)
  }

  /** Inform another `CommunicatingActor` that some action was attempted but that the attempt failed.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` to notify of the failure.
    * @param content is an action expression defining the action(s) that were attempted.
    * @param reason is a proposition indicating the reason for the failure.
    */
  def failure(conversationId: UUID, receiver: ActorRef, content: Any, reason: (Beliefs) => Boolean): Unit = {
    receiver ! Failure(conversationId, content, reason)
  }

  /** Inform another `CommunicatingActor` that some proposition is true.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` to inform that the `proposition` is true.
    * @param proposition is a proposition that the `CommunicatingActor` believes to be true, and intends that the
    *                    `receiver` also comes to believe to be true.
    * @note The `inform` act indicates that the `CommunicatingActor`:
    *
    *       1. believes that the `proposition` is true,
    *
    *       2. intends for the `receiver` to also believe that the `proposition` is true,
    *
    *       3. does not already believe that the `receiver` has any knowledge of the truth of the proposition.
    *
    *       Properties 1 and 2 require that the `CommunicatingActor` is "sincere." Property 3 determines whether the
    *       `CommunicatingActor` should use the `confirm` vs `inform` vs `disconfirm` action. Note that whether or
    *       not the `receiver` does indeed come to believe that the proposition is true will depend on the beliefs of
    *       the `receiver` concerning the sincerity and reliability of the `CommunicatingActor` sending the
    *       [[acl.acts.Inform `Inform`]] message.
    */
  def inform(conversationId: UUID, receiver: ActorRef, proposition: Beliefs): Unit = {
    receiver ! Inform(conversationId, proposition)
  }

  /** Inform another `CommunicatingActor` whether or not a `proposition` is true.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` to notify whether or not the `proposition` is true.
    * @param proposition is a proposition that the `CommunicatingActor` believes to be true, and intends that the
    *                    `receiver` also comes to believe to be true.
    * @note The `informIf` act is an abbreviation for informing a `receiver` whether or not a `proposition` is
    *       believed. Note that the `CommunicatingActor` enacting `informIf` will actually perform a standard
    *       `inform` act.  The content of the [[acl.acts.Inform `Inform`]] message will depend on `CommunicatingActor`
    *       beliefs. Specifically, if the `CommunicatingActor` believes the `proposition` is true, then it will
    *       `inform` the `receiver` that the `proposition` is true; if the `CommunicatingActor` believes the
    *       `proposition` is false, then it will `inform` the `receiver` that the `proposition` is false.
    *
    *       It may not be possible for the `CommunicatingActor` to perform the `informIf` act. For example, the
    *       `CommunicatingActor` may have no knowledge about the `proposition` in question; or will not permit the
    *       `receiver` to know the truth value of the `proposition` in order to avoid revealing private information.
    *
    *       Finally, while the `informIf` act can be planned or requested by a `CommunicatingActor` the `informIf` act
    *       can not be performed directly, but only upon receipt of an [[acl.acts.InformIf `InformIf`]] message.
    */
  def informIf(conversationId: UUID, receiver: ActorRef, proposition: Beliefs): Unit = {
    ???
  }

  /** Inform another `CommunicatingActor` of object(s) satisfying some descriptor.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` to notify whether or not the `proposition` is true.
    * @param content is a collection of objects matching some `descriptor`.
    * @tparam A is the type of an element in the `content` collection.
    */
  def informRef[A](conversationId: UUID, receiver: ActorRef, content: immutable.Iterable[A]): Unit = {
    receiver ! InformRef(conversationId, content)
  }

  /** Inform another `CommunicatingActor` of an object satisfying some descriptor.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` to notify whether or not the `proposition` is true.
    * @param content is an object matching some `descriptor`.
    * @tparam A is the type of desired object.
    */
  def informRef[A](conversationId: UUID, receiver: ActorRef, content: A): Unit = {
    receiver ! InformRef(conversationId, content)
  }

  /** Inform another `CommunicatingActor` that a message it sent was not understood.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the `CommunicatingActor` that sent the `message`.
    * @param message is the [[acl.acts.CommunicativeAct `CommunicativeAct`]] that was not understood.
    * @param reason is a proposition indicating the reason that the `message` was not understood.
    */
  def notUnderstood(conversationId: UUID,
                    receiver: ActorRef,
                    message: CommunicativeAct,
                    reason: (Beliefs) => Boolean): Unit = {
    receiver ! NotUnderstood(conversationId, message, reason)
  }

  /** Propagate a message to another `CommunicatingActor` satisfying a descriptor.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of
    *                       [[acl.acts.CommunicativeAct `CommunicativeAct`]] that together form a conversation.
    * @param receiver is the `CommunicatingActor` to whom the [[acl.acts.Propagate `Propagate`]] message should be sent.
    * @param message is the embedded [[acl.acts.CommunicativeAct `CommunicativeAct`]] which is being propagated.
    * @param descriptor is a proposition denoting a collection of actors to whom the [[acl.acts.Propagate `Propagate`]]
    *                   message should be sent by the `receiver`.
    * @param constraint is a proposition describing a termination condition for the propagation of the `message`.
    * @note The `propagate` act works as follows:
    *
    *       1. The `CommunicatingActor` requests the `receiver` to treat the embedded message in the
    *       received [[acl.acts.Propagate `Propagate`]] message as if it is was directly sent from the
    *       `CommunicatingActor`, that is, as if the `CommunicativeActor` performed the embedded communicative act
    *       directly to the `receiver`.
    *
    *       2. The `CommunicatingActor` wants the `receiver` to identify other actors denoted by the given `descriptor`
    *       and to send the received [[acl.acts.Propagate `Propagate`]] message to them.
    *
    *       This communicative act is designed for delivering messages through federated agents by creating a chain
    *       (or tree) of [[acl.acts.Propagate `Propagate`]] messages.
    */
  def propagate(conversationId: UUID,
                receiver: ActorRef,
                message: CommunicativeAct,
                descriptor: (ActorRef) => Boolean,
                constraint: (Beliefs) => Boolean): Unit = {
    receiver ! Propagate(conversationId, message, descriptor, constraint)
  }

  /** Submit a proposal to perform certain actions given certain preconditions.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is a collection of actors receiving the [[acl.acts.Propose `Propose`]] message.
    * @param content is an action expression representing the action that the `CommunicatingActor` is proposing to
    *                perform.
    * @param precondition is a proposition indicating the conditions for the action to be performed.
    */
  def propose(conversationId: UUID,
              receiver: ActorRef,
              content: Any,
              precondition: (Beliefs) => Boolean): Unit = {
    receiver ! Propose(conversationId, content, precondition)
  }

  /** Request another `CommunicatingActor` to send a message to a collection of other actors matching a given
    * descriptor (possibly subject to some constraint).
    *
    * @param conversationId is an expression used to identify an ongoing sequence of
    *                       [[acl.acts.CommunicativeAct `CommunicativeAct`]] that together form a conversation.
    * @param receiver is the `CommunicatingActor` to whom the [[acl.acts.Proxy `Proxy`]] message should be sent.
    * @param message is the embedded [[acl.acts.CommunicativeAct `CommunicativeAct`]] which is being proxied.
    * @param descriptor is a proposition denoting a collection of actors to whom the [[acl.acts.Proxy `Proxy`]] message
    *                   should be sent by the `receiver`.
    * @param constraint is a proposition constraining the proxying of the `message`.
    * @note The `CommunicatingActor` informs the `receiver` that it wants the `receiver` to identify actors that
    *       satisfy the given `descriptor` and forward them the embedded `message`.
    */
  def proxy(conversationId: UUID,
            receiver: ActorRef,
            message: CommunicativeAct,
            descriptor: (ActorRef) => Boolean,
            constraint: (Beliefs) => Boolean): Unit = {
    receiver ! Proxy(conversationId, message, descriptor, constraint)
  }

  /** Query a collection of actors in order to ascertain the truth value of some proposition.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the collection of [[acl.CommunicatingActor `CommunicatingActor`]] receiving the query.
    * @param proposition is a proposition about which the `CommunicatingActor` is ignorant (i.e., has no knowledge of
    *                    its truth value).
    */
  def queryIf(conversationId: UUID, receiver: ActorRef, proposition: Beliefs): Unit = {
    receiver ! QueryIf(conversationId, proposition)
  }

  /** Query a collection of actors regarding object(s) that matching a given descriptor.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the [[acl.CommunicatingActor `CommunicatingActor`]] receiving the query.
    * @param descriptor is a function describing some required characteristics of an object.
    * @param selector is a rule for choosing an object from the collection of objects satisfying the `descriptor`.
    * @tparam A is the type of desired object.
    * @note `queryRef` is the act of asking the `receiver` to inform the `CommunicatingActor` of some subset of
    *       objects matching the provided `descriptor`. The `CommunicatingActor` performing the `queryRef` act is
    *       assumed
    *
    *       - not to know which object(s) match the `descriptor`, and,
    *
    *       - believes that the other [[acl.CommunicatingActor `CommunicatingActor`]] can inform on the object(s).
    *
    *       Note that the exact subset of objects matching the provided `descriptor` that are informed by `receiver`
    *       is determined by the `selector`.  Typically, the `selector` will be a behavioral rule used by the
    *       `CommunicatingActor` to choose a particular alternative from some set of options.
    */
  def queryRef[A : TypeTag](conversationId: UUID,
                            receiver: ActorRef,
                            descriptor: (A) => Boolean,
                            selector: Option[(immutable.Iterable[A]) => A]): Unit = {
    receiver ! QueryRef(conversationId, descriptor, selector)
  }

  /** The action of one `CommunicatingActor` refusing to perform a request and explaining the reason for the
    * refusal.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is a collection of actors receiving the [[acl.acts.Refuse `Refuse`]] message.
    * @param request is the [[acl.acts.Request `Request]] that the `CommunicatingActor` can no longer perform.
    * @param reason is a proposition indicating the reason that the `request` is being refused.
    * @note The `refuse` act allows a `CommunicatingActor` to inform the `receiver` that it is no longer possible for
    *       it to perform a previously agreed `request`.
    */
  def refuse(conversationId: UUID,
             receiver: ActorRef,
             request: Request,
             reason: (Beliefs) => Boolean): Unit = {
    receiver ! Refuse(conversationId, request, reason)
  }

  /** The action of rejecting a previously submitted proposal to perform an action.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is a collection of actors receiving the [[acl.acts.RejectProposal `RejectProposal`]] message.
    * @param proposal is a previously received [[acl.acts.Propose `Propose`]] message that is being rejected.
    * @param reason is a proposition indicating the reason for the rejection.
    * @note `rejectProposal` is a general-purpose rejection of a previously received [[acl.acts.Propose `Propose`]]
    *       message. The `CommunicatingActor` sending the [[acl.acts.RejectProposal `RejectProposal`]] message informs
    *       the `receiver` that it has no intention that the `receiver` performs the given actions as defined in the
    *       `content`. The additional proposition `reason` indicates the reason that the `CommunicatingActor` rejected
    *       the `proposal`.
    */
  def rejectProposal(conversationId: UUID,
                     receiver: ActorRef,
                     proposal: Propose,
                     reason: (Beliefs) => Boolean): Unit = {
    receiver ! RejectProposal(conversationId, proposal, reason)
  }

  /** The `CommunicatingActor` requests the receiver to perform some action.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the collection of [[acl.CommunicatingActor `CommunicatingActor`]] that are being requested to
    *                 perform action(s) specified in the `content`.
    * @param content An action expression denoting the action(s) to be done.
    * @note The `CommunicatingActor` is requesting the `receiver` to perform some action. The `content` of the
    *       [[acl.acts.Request `Request]] message is a description of the action to be performed in a language that the
    *       `receiver` understands.
    *
    *       An important use of the `request` act is to build composite conversations between `CommunicatingActor`,
    *       where the actions that are the object of the `request` act are themselves instances of
    *       [[acl.acts.CommunicativeAct `CommunicativeAct`]].
    */
  def request(conversationId: UUID, receiver: ActorRef, content: Any): Unit = {
    receiver ! Request(conversationId, content)
  }

  /** The `CommunicatingActor` requests the receiver to perform some action when some given precondition becomes true.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the collection of [[acl.CommunicatingActor `CommunicatingActor`]] that are being requested to
    *                 perform action(s) specified in the `content`.
    * @param content An action expression denoting the action(s) to be done.
    * @param precondition A proposition indicating the conditions for the action to be performed.
    * @note The `requestWhen` act allows a `CommunicatingActor` to inform another actor that a certain action should
    *       be performed as soon as a given precondition, expressed as a proposition, becomes true.
    *
    *       The `CommunicatingActor` receiving a [[acl.acts.RequestWhen `RequestWhen`]] message should either refuse to
    *       take on the commitment or should arrange that the action will be performed when the precondition becomes
    *       true. The commitment persists until...
    *
    *       - the precondition becomes true,
    *
    *       - the `CommunicatingActor` that sent the [[acl.acts.RequestWhen `RequestWhen`]] cancels the request by
    *       sending a [[acl.acts.Cancel `Cancel`]] message,
    *
    *       - the `CommunicatingActor` determines that it can no longer honour the commitment in which case it sends a
    *       [[acl.acts.Refuse `Refuse`]] message.
    */
  def requestWhen(conversationId: UUID,
                  receiver: ActorRef,
                  content: Any,
                  precondition: (Beliefs) => Boolean): Unit = {
    receiver ! RequestWhen(conversationId, content, precondition)
  }

  /** The `CommunicatingActor` requests the receiver to perform some action when some given precondition becomes true
    * and thereafter each time the proposition becomes true again.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is the collection of [[acl.CommunicatingActor `CommunicatingActor`]] that are being requested to
    *                 perform action(s) specified in the `content`.
    * @param content An action expression denoting the action(s) to be done.
    * @param precondition A proposition indicating the conditions for the action to be performed.
    * @note The `requestWhenever` act allows a `CommunicatingActor` to inform another actor that a certain action should
    *       be performed as soon as a given precondition, expressed as a proposition, becomes true, and that, after
    *       that, if the precondition should subsequently become false, the action will be repeated as soon as the
    *       precondition becomes true again.
    *
    *       The [[acl.acts.RequestWhenever `RequestWhenever`]] message represents a persistent commitment to re-evaluate
    *       the given precondition and to take action when its value changes. The `CommunicatingActor` that sent the
    *       [[acl.acts.RequestWhenever `RequestWhenever`]] message can cancel the commitment by sending a
    *       [[acl.acts.Cancel `Cancel`]] message.
    */
  def requestWhenever(conversationId: UUID,
                      receiver: ActorRef,
                      content: Any,
                      precondition: (Beliefs) => Boolean): Unit = {
    receiver ! RequestWhenever(conversationId, content, precondition)
  }

  /** Request another `CommunicatingActor` to notify the value of a reference whenever the object identified by the
    * reference changes.
    *
    * @param conversationId is an expression used to identify an ongoing sequence of communicative acts that together
    *                       form a conversation.
    * @param receiver is [[acl.CommunicatingActor `CommunicatingActor`]] that is to receive the subscription request.
    * @param descriptor is a function describing some required characteristics of the reference object.
    */
  def subscribe(conversationId: UUID, receiver: ActorRef, descriptor: (Any) => Boolean): Unit = {
    receiver ! Subscribe(conversationId, descriptor)
  }
  
}

