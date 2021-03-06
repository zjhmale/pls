(* days of the week *)

Inductive day : Type :=
  | monday : day
  | tuesday : day
  | wednesday : day
  | thursday : day
  | friday : day
  | saturday : day
  | sunday : day.

Definition next_weekday (d:day) : day :=
  match d with
    | monday => tuesday
    | tuesday => wednesday
    | wednesday => thursday
    | thursday => friday
    | friday => monday
    | saturday => monday
    | sunday => monday
  end.

Eval compute in (next_weekday friday).
Eval compute in (next_weekday (next_weekday saturday)).

Example test_next_weekday:
  (next_weekday (next_weekday saturday)) = tuesday.

Proof.
  simpl.
  reflexivity.
Qed.

(* Booleans *)

Inductive bool : Type :=
  | true : bool
  | false : bool.

Definition negb (b:bool) : bool :=
  match b with
    | true => false
    | false => true
  end.

Definition andb (b1:bool) (b2:bool) : bool :=
  match b1 with
    | true => b2
    | false => false
  end.

Definition orb (b1:bool) (b2:bool) : bool :=
  match b1 with
    | true => true
    | false => b2
  end.

Example test_orb1: (orb true false) = true.
Proof. reflexivity. Qed.
Example test_orb2: (orb false false) = false.
Proof. reflexivity. Qed.
Example test_orb3: (orb false true) = true.
Proof. reflexivity. Qed.
Example test_orb4: (orb true true) = true.
Proof. reflexivity. Qed.

(* example and proof is just like unit test in coq *)

(* exercise1 *)

Definition nandb (b1:bool) (b2:bool) : bool :=
  negb (andb b1 b2).

Example test_nandb1: (nandb true false) = true.
Proof. reflexivity. Qed.
Example test_nandb2: (nandb false false) = true.
Proof. reflexivity. Qed.
Example test_nandb3: (nandb true true) = false.
Proof. reflexivity. Qed.
Example test_nandb4: (nandb false true) = true.
Proof. reflexivity. Qed.

Definition andb3 (b1:bool) (b2:bool) (b3:bool) : bool :=
  match b1 with
    | true => andb b2 b3
    | false => false
  end.

Example test_andb31: (andb3 true true true) = true.
Proof. reflexivity. Qed.
Example test_andb32: (andb3 false true true) = false.
Proof. reflexivity. Qed.
Example test_andb33: (andb3 true false true) = false.
Proof. reflexivity. Qed.
Example test_andb34: (andb3 true true false) = false.
Proof. reflexivity. Qed.

(* function types *)

Check true.
Check (negb true).
Check negb.

(* Numbers *)

(* module system 一般都是为了来区分名字空间 *)

Module Playground1.

  Inductive nat : Type :=
    | O : nat
    | S : nat -> nat.

  Definition pred (n : nat) : nat :=
    match n with
      | O => O
      | S n' => n'
    end.

End Playground1.

Definition minustwo (n : nat) : nat :=
  match n with
    | O => O
    | S O => O
    | S (S n') => n'
  end.

Check (S (S (S (S O)))).
Eval compute in (minustwo 4).

Check S.
Check pred.
Check minustwo.

Fixpoint evenb (n:nat) : bool :=
  match n with
    | O => true
    | S O => false
    | S (S n') => evenb n'
  end.

Definition oddb (n:nat) : bool := negb (evenb n).            

Example test_oddb1: (oddb (S O)) = true.
Proof. reflexivity. Qed.
Example test_oddb2: (oddb (S (S (S (S O))))) = false.
Proof. reflexivity. Qed.

Module Playground2.

  Fixpoint plus (n:nat) (m:nat) : nat :=
    match n with
      | O => m
      | S n' => S (plus n' m)
    end.

  Fixpoint mult (n m : nat) : nat :=
    match n with
      | O => O
      | S n' => plus m (mult n' m)
    end.

  Fixpoint minus (n m : nat) : nat :=
    match n, m with
      | O, _ => O
      | S _, O => n
      | S n', S m' => minus n' m'
    end.

End Playground2.

Eval compute in (plus (S (S (S O))) (S (S O))).

(* S O 就是自然数1 也就是0的后继 *)

Fixpoint exp (base power : nat) : nat :=
  match power with
    | O => S O
    | S p => mult base (exp base p)
  end.

Fixpoint factorial (n:nat) : nat :=
  match n with
    | O => S O
    | S n' => mult n (factorial n')
  end.

Example test_factorial1: (factorial 3) = 6.
Proof. reflexivity. Qed.
Example test_factorial2: (factorial 5) = (mult 10 12).
Proof. reflexivity. Qed.

Notation "x + y" := (plus x y)
                      (at level 50, left associativity)
                      : nat_scope.
Notation "x - y" := (minus x y)
                      (at level 50, left associativity)
                      : nat_scope.
Notation "x × y" := (mult x y)
                       (at level 50, left associativity)
                       : nat_scope.

Check ((0 + 1) + 1).

(* beq_nat 判断两个自然数相等 ble_nat 判断两个自然数是不是小于等于关系 *)

Fixpoint beq_nat (n m : nat) : bool :=
  match n with
    | O => match m with
             | O => true
             | S m' => false
           end
    | S n' => match m with
                | O => false
                | S m' => beq_nat n' m'
              end
  end.

Fixpoint ble_nat (n m : nat) : bool :=
  match n with
    | O => true
    | S n' =>
      match m with
        | O => false
        | S m' => ble_nat n' m'
      end
  end.

Example test_beq_nat: (beq_nat 2 2) = true.
Proof. reflexivity. Qed.
Example test_ble_nat1: (ble_nat 2 2) = true.
Proof. reflexivity. Qed.
Example test_ble_nat2: (ble_nat 2 4) = true.
Proof. reflexivity. Qed.
Example test_ble_nat3: (ble_nat 4 2) = false.
Proof. reflexivity. Qed.

Definition blt_nat (n m : nat) : bool :=
  andb (negb (beq_nat n m)) (ble_nat n m).

Example test_blt_nat1: (blt_nat 2 2) = false.
Proof. reflexivity. Qed.
Example test_blt_nat2: (blt_nat 2 4) = true.
Proof. reflexivity. Qed.
Example test_blt_nat3: (blt_nat 4 2) = false.
Proof. reflexivity. Qed.

Theorem plus_O_n : forall n : nat, 0 + n = n.
Proof.
  intros n. reflexivity. Qed.

(* inc 操作就是得到当前自然数的后继 *)

Theorem plus_1_1 : forall n : nat, 1 + n = S n.
Proof.
  intros n. reflexivity. Qed.

Theorem mult_0_1 : forall n : nat, 0 * n = 0.
Proof.
  intros n. reflexivity. Qed.

Theorem plus_id_example : forall n m : nat,
                            n = m ->
                            n + n = m + m.

Proof.
  intros n m H.
  rewrite <- H.
  reflexivity.
Qed.

Theorem plus_id_exercise : forall n m o : nat,
                             n = m -> m = o -> n + m = m + o.

Proof.
  intros n m o H1 H2.
  rewrite -> H1.
  rewrite <- H2.
  reflexivity.
Qed.
  