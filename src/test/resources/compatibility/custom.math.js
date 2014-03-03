less.tree.functions.add = function (a, b) {
    return new(less.tree.Dimension)(a.value + b.value);
};
less.tree.functions.increment = function (a) {
    return new(less.tree.Dimension)(a.value + 1);
};