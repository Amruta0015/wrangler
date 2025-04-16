@Override
public TokenGroup visitByteSizeArg(DirectivesParser.ByteSizeArgContext ctx) {
    String text = ctx.getText();
    return new TokenGroup(new ByteSize(text));
}

@Override
public TokenGroup visitTimeDurationArg(DirectivesParser.TimeDurationArgContext ctx) {
    String text = ctx.getText();
    return new TokenGroup(new TimeDuration(text));
}
