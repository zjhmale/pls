(ns clojure-pl.logic.trs
  (:refer-clojure :exclude [== reify inc])
  (:use [clojure.core.logic]))

;; 一个relation或者"目标"(函数的关系式对应物)不区分参数与返回值 firsto resto conso
;; 因而使得"逆向"运算成为可能 从而对一个已知的"返回"值可以得到一个或几个可能的参数
;; conso这一类relation不区分参数与返回值 将"返回值"作为额外的参数放在参数表的最后一个位置

;; a relation is just a function returns a goal result
;; s# is successful goal
;; f# is failed goal
;; run 里返回的list里面的值就是fresh变量q所associated的值

(= (run* [q]
     u#)
   (lazy-seq '()))
;; since u# fails,
;; and because the expression (run∗ (q) g ...) has the value () if any goal in g ... fails.

(= (run* [q]
     (== s# q))
   (lazy-seq `(~s#)))
;; unification is alwayse success and q is associated with s# goal

(= (run* [q]
     u#
     (== true q))
   (lazy-seq '()))
;; because the expression (run∗ (q) g ... (≡ true q)) has the value () if the goals g ... fail.

(= (run* [q]
     s#
     (== true q))
   (lazy-seq '(true)))
;; because the expression (run∗ (q) g ... (≡ true q)) associates true with q if the goals g ... and (≡ true q) succeed.

(= (run* [q]
     s#
     (== false q))
   (lazy-seq '(false)))
;; (== false x) will be succeed depend on the value of x

(let [x true]
  (== false x))
;; unify will failed case x is not false

(let [x false]
  (== false x))
;; unify will success case x is false

(= (run* [x]
     (let [x true]
       (== true x)))
   (lazy-seq '(_0)))
;; 因为中间的relation返回的goal是success的
;; 而let又是local bind是一个lambda abstraction
;; 所以外面的fresh变量可以为任何值 就用占位符代替了

(= (run* [x]
     (let [x true]
       (== false x)))
   (lazy-seq '()))
;; 中间的relation的goal是失败 所以外面的fresh变量没有归一值

(= (run* [q]
     (fresh [x]
       (== true x)
       (== true q)))
   (lazy-seq '(true)))
;; because ‘(fresh (x ...) g ...)’ binds fresh variables to x ...
;; and succeeds if the goals g ... succeed.
;; (== v x) succeeds when x is fresh. q, x are all fresh variable

;; the law of fresh => If x is fresh, then (== v x) succeeds and associates x with v.
;; if a fresh variable is associated it's no longer fresh and can not be associated any more.
;; the law of == (unification) => (≡v w)isthesameas(≡w v).

(= (run* [q]
     (fresh [x]
       (== x true)
       (== q true)))
   (lazy-seq '(true)))
;; because the order of arguments to == does not matter.

(= (run* [q]
     s#)
   (lazy-seq '(_0)))
;; value associated with x '_0 is a symbol representing a fresh variable.

(= (run* [x]
     (let [x false]
       (fresh [x]
         (== true x))))
   (lazy-seq '(_0)))
;; sincethex in (== true x) is the one introduced by the fresh expression
;; it is neither the x introduced in the run expression
;; nor the x introduced in the lambda expression(let).

(= (run* [r]
     (fresh [x y]
       (== (cons x (cons y '())) r)))
   (lazy-seq '((_0 _1))))
;; For each different fresh variable there is a symbol with an underscore followed by a numeric subscript.
;; This entity is not a variable but rather is a way of showing that the variable was fresh.
;; We say that such a variable has been reified.

(= (run* [r]
     (fresh [x]
       (let [y x]
         (fresh [x]
           (== (cons x (cons y (cons x '()))) r)))))
   (run* [r]
     (fresh [x]
       (let [y x]
         (fresh [x]
           (== (cons y (cons x (cons y '()))) r)))))
   (lazy-seq '((_0 _1 _0))))
;; Within the inner fresh, x and y are different variables,
;; and since they are still fresh, they get different reified names.
;; Reifying r’s value reifies the fresh variables in the order in which they appear in the list.

(= (run* [q]
     (== false q)
     (== true q))
   (lazy-seq '()))
;; The first goal (== false q) succeeds, associating false with q;
;; true cannot then be associated with q, since q is no longer fresh.

(= (run* [q]
     (== false q)
     (== false q))
   (lazy-seq '(false)))
;; In order for the run to succeed, both (== false q) and (== false q) must succeed
;; The first goal succeeds while associating false with the fresh variable q.
;; The second goal succeeds because although q is no longer fresh, false is already associated with it.

(= (run* [q]
     (let [x q]
       (== true x)))
   (lazy-seq '(true)))
;; because q and x are the same.

(= (run* [q]
     (fresh [x]
       (== x q)))
   (lazy-seq '(_0)))
;; because q starts out fresh and then q gets whatever association that x gets,
;; but both x and q remain fresh.
;; When one fresh variable is associated with another,
;; we say they co-refer or share.

(= (run* [q]
     (fresh [x]
       (== true x)
       (== x q)))
   (lazy-seq '(true)))
;; co-refer law

(= (run* [q]
     (fresh [x]
       (== x q)
       (== true x)))
   (lazy-seq '(true)))
;; because the first goal ensures that whatever association x gets, q also gets.
;; co-refer law

(= (run* [q]
     (fresh [x]
       (== (= x q) q)))
   (lazy-seq '(false)))

(= (run* [q]
     (let [x q]
       (fresh [q]
         (== (= x q) x))))
   (lazy-seq '(false)))
;; Every variable introduced by fresh (or run) is different from every other variable introduced by fresh (or run).

(= (run* [q]
     (conde
       (u# s#)
       (s# u#)))
   (lazy-seq '()))

(= (run* [q]
     (conde
       (s# s#)
       (s# u#)))
   (lazy-seq '(_0)))

(= (run* [q]
     (conde
       (u# s#)
       (s# s#)))
   (lazy-seq '(_0)))

(= (run* [q]
     (conde
       (s# s#)
       (s# s#)))
   (lazy-seq '(_0 _0)))
;; conde is actually the miniKanren's condi. Core.logic offers no conde.
;; This means the order of results may not match what is shown when you use conde with miniKanren.
;; conde does not support defining an else clause. Just use a (s# ...) at the end of your conde.

(= (run* [x]
     (conde
       ((== 'olive x) s#)
       ((== 'oil x) s#)
       (s# u#)))
   (lazy-seq '(olive oil)))

(= (run* [x]
     (conde
       ((== 'olive x) u#)
       ((== 'oil x) s#)
       (s# u#)))
   (lazy-seq '(oil)))

(= (run* [x]
     (conde
       ((== 'olive x) s#)
       ((== 'oil x) s#)
       (s# s#)))
   (lazy-seq '(olive oil _0)))
;; because (== 'olive x) succeeds; therefore, the answer is s#.
;; The s# preserves the association of x to 'olive.
;; To get the second value, we pretend that (== 'olive x) fails;
;; this imagined failure refreshes x. Then (== 'oil x) succeeds.
;; The s# preserves the association of x to 'oil.
;; We then pretend that (== 'oil x) fails, which once again refreshes x.
;; Since no more goals succeed, we are done.

;; the law of conde => To get more values from conde,
;; pretend that the successful conde line has failed,
;; refreshing all variables that got an association from that line.

;; the e in conde stands for every line, since every line can succeed.

(= (run 1 [x]
        (conde
          ((== 'olive x) s#)
          ((== 'oil x) s#)
          (s# u#)))
   (lazy-seq '(olive)))
;; run 1 produces at most one value.

(= (run* [x]
     (conde
       ((== 'virgin x) u#)
       ((== 'olive x) s#)
       (s# s#)
       ((== 'oil x) s#)
       (s# u#)))
   (run* [x]
     (conde
       ((== 'olive x) s#)
       ((s# s#))
       ((== 'oil x) s#)
       (s# u#)))
   (lazy-seq '(olive _0 oil)))
;; once the first conde line fails, it is as if that line were not there.

(= (run 2 [x]
        (conde
          ((== 'extra x) s#)
          ((== 'virgin x) u#)
          ((== 'olive x) s#)
          ((== 'oil x) s#)
          (s# u#)))
   (lazy-seq '(extra olive)))
;; just get first two values

(= (run* [r]
     (fresh [x y]
       (== 'split x)
       (== 'pea y)
       (== (cons x (cons y '())) r)))
   (lazy-seq '((split pea))))

(run* [r]
  (fresh [x y]
    (conde
      ((== 'split x) (== 'pea y))
      ((== 'navy x) (== 'bean y))
      (s# u#))
    (== (cons x (cons y '())) r)))

(run* [r]
  (fresh [x y]
    (conde
      ((== 'split x) (== 'pea y))
      ((== 'navy x) (== 'bean y))
      (s# u#))
    (== (cons x (cons y (cons 'soup '()))) r)))

(defn teacupo
  [x]
  (conde
    ((== 'tea x) s#)
    ((== 'cup x) s#)
    (s# u#)))

(= (run* [x]
     (teacupo x))
   (lazy-seq '(tea cup)))

(= (run* [r]
     (fresh [x y]
       (conde
         ((teacupo x) (== true y) s#)
         ((== false x) (== true y))
         (s# u#))
       (== (cons x (cons y ())) r)))
   (lazy-seq '((false true) (tea true) (cup true))))
;; From (teacupo x), x gets two associations, and from (== false x), x gets one association.

(= (run* [r]
     (fresh [x y z]
       (conde
         ((== y x) (fresh [x] (== z x)))
         ((fresh [x] (== y x)) (== z x))
         (s# u#))
       (== (cons y (cons z ())) r)))
   (lazy-seq '((_0 _1) (_0 _1))))
;; it looks like both occurrences of _0 have come from the same variable
;; and similarly for both occurrences of _1

(= (run* [r]
     (fresh [x y z]
       (conde
         ((== y x) (fresh [x] (== z x)))
         ((fresh (x) (== y x)) (== z x))
         (s# u#))
       (== false x)
       (== (cons y (cons z ())) r)))
   (lazy-seq '((false _0) (_0 false))))
;; clearly shows that the two occurrences of _0 and _1 in the previous frame represent different variables.
;; fresh 会开一个新的scope来放逻辑变量 所以第一个y是和外层的x associate 第一个z适合fresh的x associate
;; 第二个y是和fresh的xassociate 第二个z是和外层的x associate

(= (run* [q]
     (== false q)
     (== true q))
   (lazy-seq '()))

(= (run* [q]
     (let [_ (== false q)
           ;; q is associated with true
           b (== true q)]
       b))
   (run* [q]
     (let [_ (== false q)
           b (== true q)]
       #(b %)))
   (lazy-seq '(true)))

(= (run* [q]
     (let [a (== false q)
           _ (== true q)]
       a))
   (lazy-seq '(false)))
;; which shows that (== true q) and (== false q) are expressions, each of whose value is a goal.
;; But, here we only treat the (== false q) expression’s value, b, as a goal.
;; == unify操作表达式返回的函数一个lambda 是run*最后回去调用的

(= (run* [q]
     (let [_ (== true q)
           b (fresh [x]
               ;; q is associated with x
               ;; and will associate any value x associated
               (== x q)
               (== false x))
           _ (conde
               ((== true q) s#)
               (s# (== false q)))]
       b))
   (lazy-seq '(false)))

(= (run* [q]
     (let [a (== true q)
           ;; q is associated with true
           _ (fresh [x]
               (== x q)
               (== false x))
           _ (conde
               ((== true q) s#)
               (s# (== false q)))]
       a))
   (lazy-seq '(true)))

(= (run* [q]
     (let [_ (== true q)
           _ (fresh [x]
               (== x q)
               (== false x))
           c (conde
               ;; q is associated with true or false
               ((== true q) s#)
               (s# (== false q)))]
       c))
   (lazy-seq '(true false)))
;; shows that (== ...), (fresh ...), and (conde ...) are expressions, each of whose value is a goal.

(= (run* [r]
     (fresh [y x]
       (== `(~y ~x) r)))
   (lazy-seq '((_0 _1))))
;; because the variables in (x y) have been introduced by fresh.

(= (run* [r]
     (fresh [v w]
       (== (let [x v
                 y w]
             `(~x ~y))
           r)))
   (lazy-seq '((_0 _1))))
;; because v and w are variables introduced by fresh.

(= (run* [r]
     (firsto '(a c o r n) r))
   (lazy-seq '(a)))
;; r is associated with 'a because 'a is the car of '(a c o r n).
;; firsto is also a relation like == fresh conde that return a goal

(= (run* [q]
     (firsto '(a c o r n) 'a)
     (== true q))
   (lazy-seq '(true)))

(= (run* [q]
     (firsto '(a c o r n) 'b)
     (== true q))
   (lazy-seq '()))
;; because a is the car of (a c o r n).
;; 只有在中间的relation的goal都是succeed的 q才会associated到unification的值

(defn caro
  [p a]
  (fresh [d]
    (== (lcons a d) p)))

(= (run* [r]
     (fresh [x y]
       (firsto `(~r ~y) x)
       (== 'pear x)))
   (run* [r]
     (fresh [x y]
       (caro `(~r ~y) x)
       (== 'pear x)))
   (lazy-seq '(pear)))
;; since x is associated with the car of (r y), which is the fresh variable r.
;; Then x is associated with pear, which in turn associates r with pear.
;; co-refer here.

(= (run* [r]
     (fresh [x y]
       (firsto '(grape raisin pear) x)
       (firsto '((a) (b) (c)) y)
       (== (lcons x y) r)))
   (lazy-seq '((grape a))))
;; Because variables introduced by fresh are values, and each argument to cons can be any value.
;; in clojure/core.logic we should use lcons to avoid ISeq cast problem

(= (run* [r]
     (fresh [v]
       (resto '(a c o r n) v)
       (firsto v r)))
   (lazy-seq '(c)))
;; The process of transforming (car (cdr l)) into (cdro l v) and (caro v r) is called unnesting.

(= (run* [r]
     (fresh [x y]
       (resto '(grape raisin pear) x)
       (firsto '((a) (b) (c)) y)
       (== (lcons x y) r)))
   (lazy-seq '(((raisin pear) a))))

(= (run* [q]
     (resto '(a c o r n) '(c o r n))
     (== true q))
   (lazy-seq '(true)))

(= (run* [q]
     (resto '(a c o r n) '(o r n))
     (== true q))
   (lazy-seq '()))
;; because (c o r n) is the cdr of (a c o r n).

(= (run* [x]
     (resto '(c o r n) `(~x ~'r ~'n)))
   (run* [x]
     (resto '(c o r n) (list x 'r 'n)))
   (lazy-seq '(o)))
;; because (o r n) is the cdr of (c o r n), so x gets associated with o.

(defn cdro
  [p d]
  (fresh [a]
    (== (lcons a d) p)))

(= (run* [l]
     (fresh [x]
       (resto l '(c o r n))
       (firsto l x)
       (== 'a x))
     (fresh [x]
       (cdro l '(c o r n))
       (firsto l x)
       (== 'a x)))
   (lazy-seq '((a c o r n))))
;; because if the cdr of l is (list 'c 'o 'r 'n), then l must be the list (list a 'c 'o 'r 'n),
;; where a is the fresh variable introduced in the definition of cdro.
;; Taking the caro of l associates the car of l with x.
;; When we associate x with 'a, we also associate a, the car of l, with 'a, so l is associated with the list (list 'a 'c 'o 'r 'n).

(= (run* [l]
     (conso '(a b c) '(d e) l))
   (lazy-seq '(((a b c) d e))))
;; since conso associates l with (cons '(a b c) '(d e)).

(= (run* [x]
     (conso x '(a b c) '(d a b c)))
   (lazy-seq '(d)))
;; Since (cons d (a b c)) is (d a b c), conso associates x with d.

(= (run* [r]
     (fresh [x y z]
       (== `(~'e ~'a ~'d ~x) r)
       (conso y `(~'a ~z ~'c) r)))
   (lazy-seq '((e a d c))))
;; because first we associate r with a list whose last element is the fresh variable x.
;; We then perform the conso, associating x with c,z with d,and y with e.

(= (run* [x]
     (conso x `(~'a ~x ~'c) `(~'d ~'a ~x ~'c)))
   (lazy-seq '(d)))
;; What value can we associate with x so that (cons x (a x c)) is (d a x c)?
;; Obviously, d is the value.

(= (run* [l]
     (fresh [x]
       (== `(~'d ~'a ~x ~'c) l)
       (conso x `(~'a ~x ~'c) l)))
   (lazy-seq '((d a d c))))
;; because l is (list 'd 'a x 'c).
;; Then when we conso x onto (list 'a x 'c), we associate x with 'd.

(= (run* [l]
     (fresh [d x y w s]
       (conso w '(a n s) s)
       (resto l s)
       (firsto l x)
       (== 'b x)
       (resto l d)
       (firsto d y)
       (== 'e y)))
   (lazy-seq '((b e a n s))))
;; l must clearly be a five element list, since s is (cdr l).
;; Since l is fresh, (cdro l s) places a fresh variable in the first position of l,
;; while associating w and (a n s) with the second position and the cdr of the cdr of l,
;; respectively. The first variable in l gets associated with x,
;; which in turn gets associated with 'b.
;; The cdr of l is a list whose car is the variable w.
;; That variable gets associated with y, which in turn gets associated with e.

(= (run* [q]
     (emptyo '(grape raisin pear))
     (== true q))
   (lazy-seq '()))

(= (run* [q]
     (emptyo '())
     (== true q))
   (lazy-seq '(true)))

(defn nullo
  [x]
  (== () x))

(= (run* [q]
     (== q '(grape raisin pear))
     (emptyo q))
   (lazy-seq '()))

(= (run* [q]
     (emptyo q))
   (run* [q]
     (nullo q))
   (lazy-seq '(())))
;; associate q with empty list

(defn eqo [x y]
  (== x y))
;; Define eqo using ==.

(= (run* [q]
     (eqo 'pear 'plum)
     (== true q))
   (lazy-seq '()))

(= (run* [q]
     (eqo 'plum 'plum)
     (== true q))
   (lazy-seq '(true)))

(defn pair? [x]
  (or (lcons? x)
      (and (coll? x)
           (seq? x))))

(lcons 'a 'b)
;; => (a . b)
;; a scheme way to create pair

(defn pairo
  [p]
  (fresh [a d]
    (conso a d p)))

(run∗ (r) (fresh (x y)
            (≡ (cons x (cons y salad)) r)))
